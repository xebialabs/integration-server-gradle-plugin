package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.domain.Provider
import ai.digital.integration.server.common.util.ProviderUtil.Companion.getFirstProvider
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
            .replace("{{EKS_CLUSTER_NAME}}", getEksClusterName(project,getFirstProvider(project)))
            .replace("{{EKS_VPC_NAME}}", getEksVpcName(project, getFirstProvider(project)))
            .replace("{{EKS_VPC_SOURCE}}", getEksVpcSource(project, getFirstProvider(project)))
            .replace("{{EKS_VPC_VERSION}}", getEksVpcVersion(project, getFirstProvider(project)))
            .replace("{{EKS_SOURCE}}", getEksSource(project, getFirstProvider(project)))
            .replace("{{EKS_VERSION}}", getEksVersion(project, getFirstProvider(project)))
            .replace("{{EKS_CLUSTER_VERSION}}", getEksClusterVersion(project, getFirstProvider(project)))

        template.writeText(configuredTemplate)

        return template.toPath()
    }

    private fun initTerraformModule() {
        TerraformUtil.execute(project, listOf("-chdir=${getResolvedTerraformAwsPath()}","init"))
    }

    private fun applyTerraformModule() {
        TerraformUtil.execute(project, listOf("-chdir=${getResolvedTerraformAwsPath()}","apply"))
    }

    private fun getProvisioner(project: Project, provider: Provider): String {
        return if (project.hasProperty("provisioner"))
            project.property("provisioner").toString()
        else
            provider.provisioner
    }

    private fun getEksClusterName(project: Project, provider: Provider): String {
        return if (project.hasProperty("eksClusterName"))
            project.property("eksClusterName").toString()
        else
            provider.eksClusterName
    }

    private fun getEksVpcName(project: Project, provider: Provider): String {
        return if (project.hasProperty("eksVpcName"))
            project.property("eksVpcName").toString()
        else
            provider.eksVpcName
    }

    private fun getEksVpcSource(project: Project, provider: Provider): String {
        return if (project.hasProperty("eksVpcSource"))
            project.property("eksVpcSource").toString()
        else
            provider.eksVpcSource
    }

    private fun getEksVpcVersion(project: Project, provider: Provider): String {
        return if (project.hasProperty("eksVpcVersion"))
            project.property("eksVpcVersion").toString()
        else
            provider.eksVpcVersion
    }

    private fun getEksSource(project: Project, provider: Provider): String {
        return if (project.hasProperty("eksSource"))
            project.property("eksSource").toString()
        else
            provider.eksSource
    }

    private fun getEksVersion(project: Project, provider: Provider): String {
        return if (project.hasProperty("eksVersion"))
            project.property("eksVersion").toString()
        else
            provider.eksVersion
    }

    private fun getEksClusterVersion(project: Project, provider: Provider): String {
        return if (project.hasProperty("eksClusterVersion"))
            project.property("eksClusterVersion").toString()
        else
            provider.eksClusterVersion
    }

    fun launchCluster() {
        initTerraformModule()
        applyTerraformModule()
    }

}