package ai.digital.integration.server.deploy.internals.cluster.operator

import ai.digital.integration.server.common.domain.providers.operator.AwsEksProvider
import ai.digital.integration.server.common.domain.providers.operator.AzureAksProvider
import ai.digital.integration.server.common.util.FileUtil
import org.apache.tools.ant.util.ProcessUtil
import org.gradle.api.Project
import java.io.File
import java.nio.file.Paths

open class AwsEksHelper(val project: Project) {

    fun launchCluster() {
        val awsEksProvider: AwsEksProvider = getProvider()
        val region = awsEksProvider.region.get()
        val sshKey = awsEksProvider.region.get()
        ProcessUtil.executeCommand(project, "aws --region ${region} ec2 create-key-pair --key-name ${sshKey}", logOutput = true)

        val awsEksClusterTemplateFile = getTemplate("operator/aws-eks/aws-eks-cluster.yaml")
        val awsEksClusterTemplate = awsEksClusterTemplateFile.readText(Charsets.UTF_8)
        ProcessUtil.executeCommand(project, "aws cloudformation validate-template --template-body ${awsEksClusterTemplate} ", logOutput = true)
    }

    fun shutdownCluster() {
        ProcessUtil.executeCommand(project, "aws --region  ${region} ec2 delete-key-pair --key-name ${sshKey}")
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

}
