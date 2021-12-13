package ai.digital.integration.server.deploy.internals.cluster.operator

import ai.digital.integration.server.common.domain.InfrastructureInfo
import ai.digital.integration.server.common.domain.providers.operator.AwsEksProvider
import ai.digital.integration.server.common.util.FileUtil

import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.common.util.YamlFileUtil
import net.sf.json.JSONObject
import net.sf.json.util.JSONTokener
import org.gradle.api.Project
import java.io.File
import java.nio.file.Paths

open class AwsEksHelper(project: Project) : OperatorHelper(project) {

    fun launchCluster() {
        val awsEksProvider: AwsEksProvider = getProvider()
        val skipExisting = awsEksProvider.skipExisting.get()
        createSshKey(skipExisting)
        createCluster(skipExisting)

        updateKubeConfig()
        checkClusterConnectivity()
        val kubeContextInfo = getKubectlHelper().getCurrentContextInfo(skip = true)
        updateInfrastructure(kubeContextInfo)

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

    private fun createSshKey(skipExisting: Boolean) {
        val shouldSkipExisting = if (skipExisting) {
            existingKeyPair()
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
                    logOutput = true)

        }
    }

    private fun existingKeyPair(): Boolean {
        val result = ProcessUtil.executeCommand(project, "aws  --region ${getProvider().region.get()} " +
                "ec2 describe-key-pairs " +
                "--key-names=${getProvider().sshKeyName.get()} " +
                "--query 'KeyPairs[0].KeyName' " +
                "--output text",
                logOutput = true)
        return result.contains(getProvider().sshKeyName.get())
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
                            "--on-failure DELETE " +
                            "--output text",
                    logOutput = true)
            checkClusterStatus(1500000, 300000, stackId, "CREATE_COMPLETE")// delay time 25 mins and sleep time 5 mins
        }
    }

    private fun existingStack(): Boolean {
        val stackCheckStatusCmd = "aws --region ${getProvider().region.get()} " +
                "cloudformation  describe-stacks" +
                " --stack-name " +
                "${getProvider().stack.get()} " +
                "--query 'Stacks[0].StackStatus' " +
                "--output text"

        val result = ProcessUtil.executeCommand(project,
                "$stackCheckStatusCmd",
                logOutput = true,
                throwErrorOnFailure = false)
        if (result == "CREATE_COMPLETE") {
            return true
        } else if (result.contains("does not exist")) {
            return false
        }
        return false
    }


    private fun checkClusterStatus(totalTimeInSec: Int, sleepTime: Long, stackId: String, status: String) {
        val awsEksProvider: AwsEksProvider = getProvider()
        stackStatus(awsEksProvider, totalTimeInSec, sleepTime, stackId, status)
    }

    private fun stackStatus(awsEksProvider: AwsEksProvider, totalTimeInSec: Int, sleepTime: Long, stackId: String, status: String) {
        val stackCheckStatusCmd = "aws --region ${awsEksProvider.region.get()} " +
                "cloudformation  list-stacks " +
                "--query 'StackSummaries[?StackId==`$stackId`].StackStatus' " +
                "--output text"
        val stackStatus = wait(status,
                stackCheckStatusCmd,
                "Stack",
                totalTimeInSec,
                sleepTime)
        if (!stackStatus) {
            throw RuntimeException("Resource STACK status is not $status")
        }
    }

    private fun clusterStatus(awsEksProvider: AwsEksProvider) {
        val clusterStatusCommand = "aws --region ${awsEksProvider.region.get()} " +
                "eks describe-cluster " +
                "--name ${awsEksProvider.clusterName.get()} " +
                "--query 'cluster.status' " +
                "--output text"

        val clusterStatus = wait("ACTIVE",
                clusterStatusCommand,
                "Cluster")

        if (!clusterStatus) {
            throw RuntimeException("Cluster is not ACTIVE")
        }
    }

    private fun nodeStatus(awsEksProvider: AwsEksProvider) {
        val nodeGroupCommand = "aws --region ${awsEksProvider.region.get()} " +
                "eks describe-nodegroup " +
                "--cluster-name ${awsEksProvider.clusterName.get()} " +
                "--nodegroup-name ${awsEksProvider.nodeGroupName.get()} " +
                "--query 'nodegroup.status' " +
                "--output text"

        val nodeGroupStatus = wait("ACTIVE",
                nodeGroupCommand,
                "Cluster")

        if (!nodeGroupStatus) {
            throw RuntimeException("Node Group is not ACTIVE")
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

    private fun updateKubeConfig() {
        val awsEksProvider: AwsEksProvider = getProvider()
        ProcessUtil.executeCommand(project,
                "aws eks --region ${awsEksProvider.region.get()} " +
                        "update-kubeconfig " +
                        "--name ${awsEksProvider.clusterName.get()}")
    }

    private fun updateCrValues() {
        val file = File(getProviderHomeDir(), OPERATOR_CR_VALUES_REL_PATH)
        val pairs: MutableMap<String, Any> = mutableMapOf(
                "spec.ingress.hosts" to arrayOf(getFqdn())
        )
        YamlFileUtil.overlayFile(file, pairs, minimizeQuotes = false)
    }

    private fun checkClusterConnectivity() {
        ProcessUtil.executeCommand(project,
                "kubectl get node")
    }

    private fun updateRoute53() {
        val templateFile = updateRoute53Json()
        val changeInfo = UpdateRoute53RecordSet(templateFile)
        checkRoute53Status(changeInfo)
    }

    private fun updateRoute53Json(): File {
        val awsRoute53TemplateFile = getTemplate("operator/aws-eks/aws-route53-record-update.json")
        val hostname = ProcessUtil.executeCommand(project,
                "kubectl get service dai-xld-nginx-ingress-controller -o=jsonpath=\"{.status.loadBalancer.ingress[*].hostname}\"")
        val hostZoneId = ProcessUtil.executeCommand(project,
                "aws elb describe-load-balancers" +
                        " --load-balancer-name " +
                        "${hostname.substring(0, 32)} " +
                        "--query LoadBalancerDescriptions[*].CanonicalHostedZoneNameID --output text")

        val awsRoute53Template = awsRoute53TemplateFile.readText(Charsets.UTF_8)
                .replace("{{HOSTNAME}}", "dualstack.$hostname")
                .replace("{{HOSTZONEID}}", hostZoneId)
        awsRoute53TemplateFile.writeText(awsRoute53Template)
        return awsRoute53TemplateFile
    }

    private fun UpdateRoute53RecordSet(awsRoute53TemplateFile: File): String {
        return ProcessUtil.executeCommand(project,
                "aws route53 " +
                        "change-resource-record-sets " +
                        "--hosted-zone-id Z0621108QZWN6SHNIF6I " +
                        "--change-batch file://${awsRoute53TemplateFile}")
    }

    private fun checkRoute53Status(route53Change: String) {
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
                    "aws --region ${awsEksProvider.region.get()}" +
                            " ec2 delete-key-pair " +
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
            checkClusterStatus(1500000, 300000, stackId, "DELETE_COMPLETE")
        }
    }

    override fun getProviderHomeDir(): String {
        return "${getOperatorHomeDir()}/deploy-operator-aws-eks"
    }

    override fun getProvider(): AwsEksProvider {
        return getProfile().awsEks
    }

    override fun getStorageClass(): String {
        return getProvider().storageClass.value("gp2").get()
    }

    private fun updateInfrastructure(infraInfo: InfrastructureInfo) {
        val file = File(getProviderHomeDir(), OPERATOR_INFRASTRUCTURE_PATH)
        val awsEksProvider: AwsEksProvider = getProvider()
        val pairs = mutableMapOf<String, Any>(
                "spec[0].children[0].apiServerURL" to infraInfo.apiServerURL!!,
                "spec[0].children[0].caCert" to infraInfo.caCert!!,
                "spec[0].children[0].accessKey" to awsEksProvider.accessKey.get(),
                "spec[0].children[0].accessSecret" to awsEksProvider.accessSecret.get(),
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
