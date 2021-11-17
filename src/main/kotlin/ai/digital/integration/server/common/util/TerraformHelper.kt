package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.domain.Provider
import ai.digital.integration.server.common.util.ProviderUtil.Companion.getFirstProvider
import org.gradle.api.Project
import java.io.File
import java.nio.file.Path
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

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
            .replace("{{EKS_CLUSTER_NAME}}", getAwsProperty(project,getFirstProvider(project),"eksClusterName"))
            .replace("{{EKS_VPC_NAME}}", getAwsProperty(project, getFirstProvider(project),"eksVpcName"))
            .replace("{{EKS_VPC_SOURCE}}", getAwsProperty(project, getFirstProvider(project),"eksVpcSource"))
            .replace("{{EKS_VPC_VERSION}}", getAwsProperty(project, getFirstProvider(project), "eksVpcVersion"))
            .replace("{{EKS_SOURCE}}", getAwsProperty(project, getFirstProvider(project), "eksSource"))
            .replace("{{EKS_VERSION}}", getAwsProperty(project, getFirstProvider(project),"eksVersion"))
            .replace("{{EKS_CLUSTER_VERSION}}", getAwsProperty(project, getFirstProvider(project),"eksClusterVersion"))

        template.writeText(configuredTemplate)

        return template.toPath()
    }

    private fun initTerraformModule() {
        TerraformUtil.execute(project, listOf("-chdir=${getResolvedTerraformAwsPath()}","init"))
    }

    private fun applyTerraformModule() {
        TerraformUtil.execute(project, listOf("-chdir=${getResolvedTerraformAwsPath()}","apply"))
    }

    private fun getAwsProperty(project: Project,provider: Provider, providerProperty: String): String {
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

}
