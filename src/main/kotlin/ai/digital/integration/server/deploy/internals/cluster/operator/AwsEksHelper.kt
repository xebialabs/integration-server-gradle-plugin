package ai.digital.integration.server.deploy.internals.cluster.operator

import ai.digital.integration.server.common.domain.InfrastructureInfo
import ai.digital.integration.server.common.domain.providers.operator.AwsEksProvider

import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.common.util.YamlFileUtil
import net.sf.json.JSONObject
import net.sf.json.util.JSONTokener
import org.gradle.api.Project
import java.io.File

open class AwsEksHelper(project: Project) : OperatorHelper(project) {

    fun launchCluster() {
        val awsEksProvider: AwsEksProvider = getProvider()
        val skipExisting = awsEksProvider.skipExisting.get()

        configureAws(awsEksProvider)
        createSshKey(skipExisting)
        createCluster(skipExisting)

        updateKubeConfig()

        val kubeContextInfo = getKubectlHelper().getCurrentContextInfo(skip = true)
        updateInfrastructure(kubeContextInfo)

        if (getStorageClass() == "aws-efs") createStorageClass(getStorageClass())

        updateControllerManager()
        updateOperatorDeployment()
        updateOperatorDeploymentCr()
        updateOperatorCrValues()
        updateCrValues()

        applyYamlFiles()
        waitForDeployment()
        waitForMasterPods()
        waitForWorkerPods()

        createClusterMetadata()
        updateRoute53()
        waitForBoot()
    }

    private fun configureAws(awsEksProvider: AwsEksProvider) {
        project.logger.lifecycle("Configure AWS access key and secret key")
        ProcessUtil.executeCommand(project,
                "aws configure set aws_access_key_id ${awsEksProvider.getAwsAccessKey()} --profile default",
                logOutput = false,
                throwErrorOnFailure = false)
        ProcessUtil.executeCommand(project,
                "aws configure set aws_secret_access_key ${awsEksProvider.getAwsSecretKey()} --profile default",
                logOutput = false,
                throwErrorOnFailure = false)
    }

    private fun createSshKey(skipExisting: Boolean) {
        val shouldSkipExisting = if (skipExisting) {
            existingSshKeyPair()
        } else {
            false
        }
        if (shouldSkipExisting) {
            project.logger.lifecycle("Skipping creation of the existing ssh key pair: {}", getProvider().sshKeyName.get())
        } else {
            project.logger.lifecycle("Create ssh key pair : {}", getProvider().sshKeyName.get())
            ProcessUtil.executeCommand(project,
                    "aws --region ${getProvider().region.get()} " +
                            "ec2 create-key-pair " +
                            "--key-name ${getProvider().sshKeyName.get()}",
                    logOutput = true,
                    throwErrorOnFailure = false)

        }
    }

    private fun existingSshKeyPair(): Boolean {
        val result: String = ProcessUtil.executeCommand(project,
                "aws  --region ${getProvider().region.get()} " +
                        "ec2 describe-key-pairs " +
                        "--key-names=${getProvider().sshKeyName.get()} " +
                        "--query 'KeyPairs[0].KeyName' " +
                        "--output text",
                logOutput = true,
                throwErrorOnFailure = false)
        if (result.contains("does not exist")) {
            return false
        } else if (result.contains(getProvider().sshKeyName.get())) {
            return true
        }
        return false
    }

    private fun createCluster(skipExisting: Boolean) {
        val shouldSkipExisting = if (skipExisting) {
            existingStack()
        } else {
            false
        }
        if (shouldSkipExisting) {
            project.logger.lifecycle("Skipping creation of the stack: {}", getProvider().stack.get())
        } else {
            project.logger.lifecycle("Create Stack  : {}", getProvider().stack.get())
            val awsEksClusterTemplateFile = getTemplate("operator/aws-eks/aws-eks-cluster.yaml")
            val awsEksProvider: AwsEksProvider = getProvider()

            val stackId = ProcessUtil.executeCommand(project,
                    "aws --region  ${awsEksProvider.region.get()} " +
                            "cloudformation create-stack " +
                            "--stack-name ${awsEksProvider.stack.get()} " +
                            "--template-body file://$awsEksClusterTemplateFile " +
                            "--capabilities CAPABILITY_IAM " +
                            "--parameters " +
                            "ParameterKey=ProjectName,ParameterValue=${awsEksProvider.stack.get()} " +
                            "ParameterKey=ClusterName,ParameterValue=${awsEksProvider.clusterName.get()} " +
                            "ParameterKey=NodeGroupName,ParameterValue=${awsEksProvider.nodeGroupName.get()} " +
                            "ParameterKey=KeyName,ParameterValue=${awsEksProvider.sshKeyName.get()} " +
                            "ParameterKey=FileSystemName,ParameterValue=${awsEksProvider.fileSystemName.get()} " +
                            "ParameterKey=NodeDesiredSize,ParameterValue=${awsEksProvider.clusterNodeCount.get()} " +
                            "ParameterKey=KubernetesVersion,ParameterValue=${awsEksProvider.kubernetesVersion.get()} " +
                            "--on-failure DELETE " +
                            "--output text",
                    logOutput = true)
            verifyClusterStatus(1500000, 300000, stackId, "CREATE_COMPLETE")// delay time 25 mins and sleep time 5 mins
        }
    }

    private fun existingStack(): Boolean {
        val stackStatusCmd = "aws --region ${getProvider().region.get()} " +
                "cloudformation  describe-stacks" +
                " --stack-name " +
                "${getProvider().stack.get()} " +
                "--query 'Stacks[0].StackStatus' " +
                "--output text"

        val result = ProcessUtil.executeCommand(project,
                "$stackStatusCmd",
                logOutput = true,
                throwErrorOnFailure = false)
        if (result == "CREATE_COMPLETE") {
            return true
        } else if (result.contains("does not exist")) {
            return false
        }
        return false
    }


    private fun verifyClusterStatus(totalTimeInSec: Int, sleepTime: Long, stackId: String, status: String) {
        val awsEksProvider: AwsEksProvider = getProvider()
        stackStatus(awsEksProvider, totalTimeInSec, sleepTime, stackId, status)
    }

    private fun stackStatus(awsEksProvider: AwsEksProvider, totalTimeInSec: Int, sleepTime: Long, stackId: String, status: String) {
        val stackStatusCmd = "aws --region ${awsEksProvider.region.get()} " +
                "cloudformation  list-stacks " +
                "--query 'StackSummaries[?StackId==`$stackId`].StackStatus' " +
                "--output text"
        val stackStatus = wait(status,
                stackStatusCmd,
                "Stack",
                totalTimeInSec,
                sleepTime)
        if (!stackStatus) {
            throw RuntimeException("Resource STACK status is not $status")
        }
    }

    private fun wait(status: String, command: String, resource: String, totalTimeInSec: Int = 3000, sleepTime: Long = 1000): Boolean {
        val expectedEndTime = System.currentTimeMillis() + totalTimeInSec // 20 mins
        while (expectedEndTime > System.currentTimeMillis()) {
            val result = ProcessUtil.executeCommand(project,
                    "$command",
                    logOutput = true,
                    throwErrorOnFailure = false)
            if (result.contains("$status")) {
                return true
            } else if (result.contains("does not exist")) {
                return false
            }
            project.logger.lifecycle("$resource resource  \"$status\" status not met, retry. ")
            Thread.sleep(sleepTime) //5 mins
        }
        return false
    }

    private fun createStorageClass(efsStorageClassName: String) {
        createStorageClassEFS(efsStorageClassName)
        getKubectlHelper().setDefaultStorageClass("gp2", efsStorageClassName)
    }

    private fun createStorageClassEFS(storageClassName: String) {
        if (!getKubectlHelper().hasStorageClass(storageClassName)) {
            project.logger.lifecycle("Create storage class: {}", storageClassName)
            val fileSystemId = getFileSystemId()
            helmInstallAwsEfs(fileSystemId, storageClassName)
        } else {
            project.logger.lifecycle("Skipping creation of the existing storage class: {}", storageClassName)
        }
    }

    private fun helmInstallAwsEfs(fileSystemId: String, storageClassName: String) {
        ProcessUtil.executeCommand(project, "helm repo add stable https://charts.helm.sh/stable", logOutput = false, throwErrorOnFailure = false)
        ProcessUtil.executeCommand(project, "helm install " +
                "$storageClassName stable/efs-provisioner " +
                "--set efsProvisioner.efsFileSystemId=${fileSystemId} " +
                "--set efsProvisioner.awsRegion=${getProvider().region.get()}",
                logOutput = false,
                throwErrorOnFailure = false)
    }

    private fun getFileSystemId(): String {
        return ProcessUtil.executeCommand(project,
                "aws --region ${getProvider().region.get()} " +
                        "cloudformation describe-stacks " +
                        "--stack-name ${getProvider().stack.get()} " +
                        "--query 'Stacks[0].Outputs[?OutputKey==`Filesystem`].OutputValue' " +
                        "--output text",
                logOutput = false,
                throwErrorOnFailure = false)
    }

    private fun updateKubeConfig() {
        val awsEksProvider: AwsEksProvider = getProvider()
        ProcessUtil.executeCommand(project,
                "aws eks --region ${awsEksProvider.region.get()} " +
                        "update-kubeconfig " +
                        "--name ${awsEksProvider.clusterName.get()}",
                throwErrorOnFailure = false)
    }

    private fun updateCrValues() {
        val file = File(getProviderHomeDir(), OPERATOR_CR_VALUES_REL_PATH)
        val pairs: MutableMap<String, Any> = mutableMapOf(
                "spec.ingress.hosts" to arrayOf(getFqdn())
        )
        YamlFileUtil.overlayFile(file, pairs, minimizeQuotes = false)
    }

    private fun updateRoute53() {
        val templateFile = updateRoute53Json()
        val changeInfo = UpdateRoute53RecordSet(templateFile)
        verifyRoute53Status(changeInfo)
    }

    private fun updateRoute53Json(): File {
        val awsRoute53TemplateFile = getTemplate("operator/aws-eks/aws-route53-record-update.json")
        val hostName = getHostName()
        val hostZoneId = getHostZoneId(hostName)

        val awsRoute53Template = awsRoute53TemplateFile.readText(Charsets.UTF_8)
                .replace("{{HOSTNAME}}", "dualstack.$hostName")
                .replace("{{HOSTZONEID}}", hostZoneId)

        awsRoute53TemplateFile.writeText(awsRoute53Template)
        return awsRoute53TemplateFile
    }

    private fun getHostZoneId(hostName: String): String {
        return ProcessUtil.executeCommand(project,
                "aws elb describe-load-balancers" +
                        " --load-balancer-name " +
                        "${hostName.substring(0, 32)} " +
                        "--query LoadBalancerDescriptions[*].CanonicalHostedZoneNameID --output text",
                logOutput = false,
                throwErrorOnFailure = false)
    }

    private fun getHostName(): String {
        return ProcessUtil.executeCommand(project,
                "kubectl get service" +
                        " dai-xld-nginx-ingress-controller " +
                        "-o=jsonpath=\"{.status.loadBalancer.ingress[*].hostname}\"",
                logOutput = false,
                throwErrorOnFailure = false)
    }

    private fun UpdateRoute53RecordSet(awsRoute53TemplateFile: File): String {
        return ProcessUtil.executeCommand(project,
                "aws route53 " +
                        "change-resource-record-sets " +
                        "--hosted-zone-id Z0621108QZWN6SHNIF6I " +
                        "--change-batch file://${awsRoute53TemplateFile}",
                logOutput = false,
                throwErrorOnFailure = false)
    }

    private fun verifyRoute53Status(route53Change: String) {
        val route53ChangeId = JSONTokener(route53Change).nextValue() as JSONObject
        val changeInfo = route53ChangeId.get("ChangeInfo") as JSONObject

        val route53GetChange = "aws route53 " +
                "get-change " +
                "--id=${changeInfo.get("Id")} " +
                "--query=ChangeInfo.Status " +
                "--output text"

        val changeStatus = wait("INSYNC",
                route53GetChange,
                "Route 53 change record set", 300000, 1000)

        project.logger.lifecycle("Route 53 Status $changeStatus")
    }

    fun shutdownCluster() {
        val awsEksProvider: AwsEksProvider = getProvider()
        project.logger.lifecycle("Undeploy operator")
        undeployCis()

        project.logger.lifecycle("PVCs are being deleted")
        getKubectlHelper().deleteAllPVCs()

        project.logger.lifecycle("Delete cluster and ssh key")
        deleteSshKey(awsEksProvider)
        deleteCluster(awsEksProvider)

        project.logger.lifecycle("Delete current context")
        getKubectlHelper().deleteCurrentContext()

    }

    private fun deleteSshKey(awsEksProvider: AwsEksProvider) {
        if (awsEksProvider.skipExisting.get()) {
            project.logger.lifecycle("Skipping deletion of the ssh key: {}", awsEksProvider.sshKeyName.get())
        } else {
            ProcessUtil.executeCommand(project,
                    "aws --region ${awsEksProvider.region.get()} " +
                            "ec2 delete-key-pair " +
                            "--key-name ${awsEksProvider.sshKeyName.get()}")
        }
    }

    private fun deleteCluster(awsEksProvider: AwsEksProvider) {
        if (awsEksProvider.skipExisting.get()) {
            project.logger.lifecycle("Skipping deletion of the stack: {}", awsEksProvider.stack.get())
        } else {
            val stackId = ProcessUtil.executeCommand(project,
                    "aws --region ${awsEksProvider.region.get()} " +
                            "cloudformation describe-stacks " +
                            "--stack-name ${awsEksProvider.stack.get()} " +
                            "--query='Stacks[0].StackId' " +
                            "--output text")
            ProcessUtil.executeCommand(project,
                    "aws --region ${awsEksProvider.region.get()}" +
                            " cloudformation delete-stack " +
                            "--stack-name ${awsEksProvider.stack.get()} " +
                            "--output text")
            verifyClusterStatus(1500000, 300000, stackId, "DELETE_COMPLETE")
        }
    }

    override fun getProviderHomeDir(): String {
        return "${getOperatorHomeDir()}/deploy-operator-aws-eks"
    }

    override fun getProvider(): AwsEksProvider {
        return getProfile().awsEks
    }

    override fun getStorageClass(): String {
        return getProvider().storageClass.getOrElse("aws-efs")
    }

    override fun getMqStorageClass(): String {
        return "gp2"
    }

    private fun updateInfrastructure(infraInfo: InfrastructureInfo) {
        val file = File(getProviderHomeDir(), OPERATOR_INFRASTRUCTURE_PATH)
        val awsEksProvider: AwsEksProvider = getProvider()
        val pairs = mutableMapOf<String, Any>(
                "spec[0].children[0].apiServerURL" to infraInfo.apiServerURL!!,
                "spec[0].children[0].caCert" to infraInfo.caCert!!,
                "spec[0].children[0].accessKey" to awsEksProvider.getAwsAccessKey(),
                "spec[0].children[0].accessSecret" to awsEksProvider.getAwsSecretKey(),
                "spec[0].children[0].regionName" to awsEksProvider.region.get(),
                "spec[0].children[0].clusterName" to awsEksProvider.clusterName.get()
        )
        YamlFileUtil.overlayFile(file, pairs)
    }

    override fun getFqdn(): String {
        return "deploy.digitalai-testing.com"
    }

    override fun getContextRoot(): String {
        return "/xl-deploy/"
    }

}
