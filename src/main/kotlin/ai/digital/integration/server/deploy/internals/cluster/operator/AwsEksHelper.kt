package ai.digital.integration.server.deploy.internals.cluster.operator

import ai.digital.integration.server.common.domain.providers.operator.AwsEksProvider
import ai.digital.integration.server.common.util.FileUtil

import ai.digital.integration.server.common.util.ProcessUtil
import org.gradle.api.Project
import java.io.File
import java.nio.file.Paths

open class AwsEksHelper(project: Project) : OperatorHelper(project) {

    fun launchCluster() {
        createSshKey()
        createCluster()
        checkClusterStatus()

    }

    private fun createSshKey() {
        ProcessUtil.executeCommand(project,
                "aws --region ${getProvider().region.get()} ec2 create-key-pair --key-name ${getProvider().sshKeyName.get()}",
                logOutput = true)
    }

    private fun createCluster() {
        val awsEksClusterTemplateFile = getTemplate("operator/aws-eks/aws-eks-cluster.yaml")
        val awsEksProvider: AwsEksProvider = getProvider()

        ProcessUtil.executeCommand(project,
                "aws --region  ${awsEksProvider.region.get()} " +
                        "cloudformation create-stack " +
                        "--stack-name ${awsEksProvider.stack.get()} " +
                        "--template-body file://$awsEksClusterTemplateFile " +
                        "--capabilities CAPABILITY_IAM " +
                        "--parameters " +
                        "ParameterKey=ProjectName,ParameterValue=${awsEksProvider.stack.get()} " +
                        "ParameterKey=ClusterName,ParameterValue=${awsEksProvider.clusterName.get()} " +
                        "ParameterKey=NodeGroupName,ParameterValue=${awsEksProvider.nodeGroupName.get()} " +
                        "ParameterKey=KeyName,ParameterValue=${awsEksProvider.sshKeyName.get()}",
                logOutput = true)
    }

    private fun checkClusterStatus() {
        val awsEksProvider: AwsEksProvider = getProvider()

        stackStatus(awsEksProvider)

        val clusterStatusCommand = "aws --region ${awsEksProvider.region.get()} " +
                "eks describe-cluster " +
                "--name ${awsEksProvider.clusterName.get()} " +
                "--query 'cluster.status'"
        val clusterStatus = ProcessUtil.executeCommand(project,
                "$clusterStatusCommand",
                logOutput = true,
                throwErrorOnFailure = false)

        project.logger.lifecycle("Cluster Status $clusterStatus")


        val nodeGroupCommand = "aws --region ${awsEksProvider.region.get()} " +
                "eks describe-nodegroup " +
                "--cluster-name ${awsEksProvider.clusterName.get()} " +
                "--nodegroup-name ${awsEksProvider.nodeGroupName.get()} " +
                "--query 'nodegroup.status'"

        val nodeGroupStatus = ProcessUtil.executeCommand(project,
                "$nodeGroupCommand",
                logOutput = true,
                throwErrorOnFailure = false)

        project.logger.lifecycle("NodeGroup Status $nodeGroupStatus")

    }

    private fun stackStatus(awsEksProvider: AwsEksProvider) {
        val stackCheckStatusCmd = "aws --region ${awsEksProvider.region.get()} " +
                "cloudformation  describe-stacks" +
                " --stack-name " +
                "${awsEksProvider.stack.get()} " +
                "--query 'Stacks[0].StackStatus'"

        val stackStatus = wait("COMPLETE",
                stackCheckStatusCmd,
                "Stack")

        if (!stackStatus) {
            val status = ProcessUtil.executeCommand(project,
                    "$stackCheckStatusCmd",
                    logOutput = true,
                    throwErrorOnFailure = false)
            throw RuntimeException("Resource STACK is not available, current status $status")
        }
    }


    private fun wait(status: String, command: String, resource: String): Boolean {
        val expectedEndTime = System.currentTimeMillis() + 1200000 // 20 mins
        while (expectedEndTime > System.currentTimeMillis()) {
            val result = ProcessUtil.executeCommand(project,
                    "$command",
                    throwErrorOnFailure = false)
            if (result.contains("$status")) {
                return true
            }
            project.logger.lifecycle("$resource resource  \"$status\" status not met, retry after 5 minutes. ")
            Thread.sleep(300000) //5 mins
        }
        return false
    }

    fun shutdownCluster() {
        val awsEksProvider: AwsEksProvider = getProvider()
        val region = awsEksProvider.region.get()
        val sshKey = awsEksProvider.sshKeyName.get()
        //ProcessUtil.executeCommand(project, "aws --region  $region ec2 delete-key-pair --key-name $sshKey")
        //ProcessUtil.executeCommand(project, "aws --region  $region cloudformation delete-stack --stack-name ${awsEksProvider.stack.get()}")
    }

    private fun getTemplate(relativePath: String): File {
        val file = File(relativePath)
        val fileStream = {}::class.java.classLoader.getResourceAsStream(relativePath)
        val resultComposeFilePath = Paths.get(getProviderWorkDir(), file.name)
        fileStream?.let {
            FileUtil.copyFile(it, resultComposeFilePath)
        }
        return resultComposeFilePath.toFile()
    }

    override fun getProviderHomeDir(): String {
        return "${getOperatorHomeDir()}/deploy-operator-aws-eks"
    }

    override fun getProvider(): AwsEksProvider {
        return getProfile().awsEks
    }

}
