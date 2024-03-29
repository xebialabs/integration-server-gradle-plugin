package ai.digital.integration.server.deploy.tasks.cluster

import ai.digital.integration.server.common.constant.ClusterProfileName
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.TerraformProviderName
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import ai.digital.integration.server.deploy.tasks.cli.RunCliTask
import ai.digital.integration.server.deploy.tasks.cluster.dockercompose.DockerComposeBasedStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.HelmBasedInstallDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.OperatorBasedInstallDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.terraform.TerraformBasedAwsEksStartDeployClusterTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class StartDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "startDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        val dependencies = listOf(
            when (val profileName = DeployClusterUtil.getProfile(project)) {
                ClusterProfileName.DOCKER_COMPOSE.profileName ->
                    DockerComposeBasedStartDeployClusterTask.NAME
                ClusterProfileName.OPERATOR.profileName ->
                    OperatorBasedInstallDeployClusterTask.NAME
                ClusterProfileName.HELM.profileName ->
                    HelmBasedInstallDeployClusterTask.NAME
                ClusterProfileName.TERRAFORM.profileName -> {
                    when (val providerName = DeployClusterUtil.getTerraformProvider(project)) {
                        TerraformProviderName.AWS_EKS.providerName ->
                            TerraformBasedAwsEksStartDeployClusterTask.NAME
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
            }
        )

        this.dependsOn(dependencies)

        this.finalizedBy(RunCliTask.NAME)
    }

    @TaskAction
    fun launch() {
        val profileName = DeployClusterUtil.getProfile(project)
        project.logger.lifecycle("Deploy Cluster profile $profileName has started.")
    }
}
