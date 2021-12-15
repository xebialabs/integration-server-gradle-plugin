package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.domain.profiles.TerraformProfile
import ai.digital.integration.server.common.domain.providers.terraform.AwsEksProvider
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import org.gradle.api.Project
import java.io.File
import java.nio.file.Path

open class TerraformHelper(val project: Project) {

    companion object {
        const val TERRAFORM_AWS_DIR_PATH = "provision/aws"
        const val TERRAFORM_AWS_FILE_PATH = TERRAFORM_AWS_DIR_PATH + "main.tf"
    }

    private fun getResolvedTerraformPath(project: Project, relativePath: String): Path {
        val terraformStream = {}::class.java.classLoader.getResourceAsStream(relativePath)
        val resultComposeFilePath = IntegrationServerUtil.getRelativePathInIntegrationServerDist(project, relativePath)
        terraformStream?.let {
            FileUtil.copyFile(it, resultComposeFilePath)
        }
        return resultComposeFilePath
    }

    private fun getTemplate(path: String): File {
        val resultComposeFilePath = DockerComposeUtil.getResolvedDockerPath(project, path)
        return resultComposeFilePath.toFile()
    }

    private fun resolveTerraformAwsPath() {
        val template = getTemplate(TERRAFORM_AWS_FILE_PATH)

        val configuredTemplate =
            template.readText(Charsets.UTF_8).replace("{{EKS_CLUSTER_NAME}}", getAwsEksProvider().clusterName.get())
                .replace("{{EKS_VPC_NAME}}", getAwsEksProvider().vpcName.get())
                .replace("{{EKS_VPC_SOURCE}}", getAwsEksProvider().vpcSource.get())
                .replace("{{EKS_VPC_VERSION}}", getAwsEksProvider().vpcVersion.get())
                .replace("{{EKS_SOURCE}}", getAwsEksProvider().source.get())
                .replace("{{EKS_VERSION}}", getAwsEksProvider().version.get())
                .replace("{{EKS_CLUSTER_VERSION}}", getAwsEksProvider().clusterVersion.get())

        template.writeText(configuredTemplate)
    }

    private fun initTerraformModule() {
        runCatching {
            resolveTerraformAwsPath()
        }.exceptionOrNull()?.let { exception ->

        }
        TerraformUtil.execute(project,
            listOf("-chdir=${getResolvedTerraformPath(project, TERRAFORM_AWS_DIR_PATH)}", "init"))
    }

    private fun applyTerraformModule() {
        TerraformUtil.execute(project,
            listOf("-chdir=${getResolvedTerraformPath(project, TERRAFORM_AWS_DIR_PATH)}", "apply"))
    }

    fun getProfile(): TerraformProfile {
        return DeployExtensionUtil.getExtension(project).clusterProfiles.terraform()
    }

    fun getAwsEksProvider(): AwsEksProvider {
        return getProfile().awsEks
    }

    fun launchCluster() {
        initTerraformModule()
        applyTerraformModule()
    }

    fun shutdownCluster() {
        //TODO: !!
    }

}
