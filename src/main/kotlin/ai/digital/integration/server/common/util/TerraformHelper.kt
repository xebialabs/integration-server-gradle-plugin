package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.constant.TerraformConstants
import org.gradle.api.Project
import java.io.File
import java.nio.file.Path

open class TerraformHelper(val project: Project) {

    companion object {
        const val terraformAwsPath = "provision/aws/main.tf"
    }

    private fun getTemplate(path: String): File {
        val resultComposeFilePath = DockerComposeUtil.getResolvedDockerPath(project, path)
        return resultComposeFilePath.toFile()
    }

    private fun getResolvedTerraformAwsPath(): Path {
        val template = getTemplate(terraformAwsPath)

        val configuredTemplate = template.readText(Charsets.UTF_8)
            .replace("{{EKS_CLUSTER_NAME}}", TerraformConstants.EKS_CLUSTER_NAME)
            .replace("{{EKS_VPC_NAME}}", TerraformConstants.EKS_VPC_NAME)
            .replace("{{EKS_VPC_SOURCE}}", TerraformConstants.EKS_VPC_SOURCE)
            .replace("{{EKS_VPC_VERSION}}", TerraformConstants.EKS_VPC_VERSION)
            .replace("{{EKS_SOURCE}}", TerraformConstants.EKS_SOURCE)
            .replace("{{EKS_VERSION}}", TerraformConstants.EKS_VERSION)
            .replace("{{EKS_CLUSTER_VERSION}}", TerraformConstants.EKS_CLUSTER_VERSION)

        template.writeText(configuredTemplate)

        return template.toPath()
    }

    private fun initTerraformModule() {
        TerraformUtil.execute(project, listOf("-chdir=${getResolvedTerraformAwsPath()}","init"))
    }

    private fun applyTerraformModule() {
        TerraformUtil.execute(project, listOf("-chdir=${getResolvedTerraformAwsPath()}","apply"))
    }

    fun launchCluster() {
        initTerraformModule()
        applyTerraformModule()
    }

}