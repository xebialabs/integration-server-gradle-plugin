package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.domain.Provider
import ai.digital.integration.server.common.util.ProviderUtil.Companion.getProviders
import org.gradle.api.Project
import java.io.File
import java.nio.file.Path
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

open class TerraformHelper(val project: Project) {

    companion object {
        const val TERRAFORM_AWS_DIR_PATH = "provision/aws"
        const val TERRAFORM_AWS_FILE_PATH = TERRAFORM_AWS_DIR_PATH + "main.tf"
    }

    private fun getResolvedTerraformPath(project: Project, relativePath: String): Path {
        val terraformStream = {}::class.java.classLoader.getResourceAsStream(relativePath)
        val resultComposeFilePath =
            IntegrationServerUtil.getRelativePathInIntegrationServerDist(project, relativePath)
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

        val configuredTemplate = template.readText(Charsets.UTF_8)
            .replace("{{EKS_CLUSTER_NAME}}", getValue("eksClusterName"))
            .replace("{{EKS_VPC_NAME}}", getValue("eksVpcName"))
            .replace("{{EKS_VPC_SOURCE}}", getValue("eksVpcSource"))
            .replace("{{EKS_VPC_VERSION}}", getValue("eksVpcVersion"))
            .replace("{{EKS_SOURCE}}", getValue("eksSource"))
            .replace("{{EKS_VERSION}}", getValue("eksVersion"))
            .replace("{{EKS_CLUSTER_VERSION}}", getValue("eksClusterVersion"))

        template.writeText(configuredTemplate)
    }

    private fun initTerraformModule() {
        runCatching {
            resolveTerraformAwsPath()
        }.exceptionOrNull()?.let { exception ->

        }
        TerraformUtil.execute(
            project,
            listOf("-chdir=${getResolvedTerraformPath(project, TERRAFORM_AWS_DIR_PATH)}", "init")
        )
    }

    private fun applyTerraformModule() {
        TerraformUtil.execute(
            project,
            listOf("-chdir=${getResolvedTerraformPath(project, TERRAFORM_AWS_DIR_PATH)}", "apply")
        )
    }

    private fun destroyTerraformModule(){
        TerraformUtil.execute(
            project,
            listOf("-chdir=${getResolvedTerraformPath(project, TERRAFORM_AWS_DIR_PATH)}", "destroy")
        )
    }

    private fun getValue(providerProperty: String): String {
        getProviders(project).find { provider -> provider.equals("aws") }
            ?.let { awsProvider ->
                return getAwsProperty(project, awsProvider, providerProperty)
            }
            ?: throw Exception("${providerProperty} property not found")
    }

    private fun getAwsProperty(project: Project, provider: Provider, providerProperty: String): String {
        return if (project.hasProperty(providerProperty))
            project.property(providerProperty).toString()
        else
            getPropertyValue(provider, providerProperty)
    }

    private fun getPropertyValue(provider: Provider, providerProperty: String): String {
        var propertyValue = ""

        provider::class.memberProperties.forEach { member ->
            if (member.name.equals(providerProperty)) {
                propertyValue = (member as KProperty1<Any, Any>).get(provider).toString()
            }
        }

        return propertyValue
    }

    fun launchCluster() {
        initTerraformModule()
        applyTerraformModule()
    }

    fun teardownCluster() {
        destroyTerraformModule()
    }

}
