package ai.digital.integration.server.deploy.tasks.cluster

import ai.digital.integration.server.common.constant.ClusterProfileName
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.TerraformProviderName
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import ai.digital.integration.server.deploy.tasks.cluster.dockercompose.DockerComposeBasedStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.OperatorBasedStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.terraform.TerraformBasedAwsEksStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.xlblueprint.XlBlueprintBasedStopDeployClusterTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class StopDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "stopDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        this.dependsOn(
            when (val profileName = DeployClusterUtil.getProfile(project)) {
                ClusterProfileName.DOCKER_COMPOSE.profileName ->
                    DockerComposeBasedStopDeployClusterTask.NAME
                ClusterProfileName.XL_BLUEPRINT.profileName ->
                    XlBlueprintBasedStopDeployClusterTask.NAME
                ClusterProfileName.OPERATOR.profileName ->
                    OperatorBasedStopDeployClusterTask.NAME
                ClusterProfileName.TERRAFORM.profileName -> {
                    when (val providerName = DeployClusterUtil.getTerraformProvider(project)) {
                        TerraformProviderName.AWS_EKS.providerName ->
                            TerraformBasedAwsEksStopDeployClusterTask.NAME
                        else -> {
                            throw IllegalArgumentException("Provided terraform provider name `$providerName` is not supported. Choose one of ${
                                TerraformProviderName.values().joinToString()
                            }")
                        }
                    }
                }
                else -> {
                    throw IllegalArgumentException("Provided profile name `$profileName` is not supported. Choose one of ${
                        ClusterProfileName.values().joinToString()
                    }")
                }
            })
    }

    @TaskAction
    fun launch() {
        val profileName = DeployClusterUtil.getProfile(project)
        project.logger.lifecycle("Deploy Cluster profile $profileName  is about to stop.")
    }
}
