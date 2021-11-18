package ai.digital.integration.server.deploy.tasks.cluster

import ai.digital.integration.server.common.constant.ClusterProfileName
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.TerraformProviderName
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import ai.digital.integration.server.deploy.tasks.cli.RunCliTask
import ai.digital.integration.server.deploy.tasks.cluster.dockercompose.DockerComposeBasedStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.OperatorBasedStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.terraform.TerraformBasedAwsEksStartDeployClusterTask
import org.gradle.api.DefaultTask
import org.gradle.internal.impldep.org.eclipse.jgit.errors.NotSupportedException

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
                    OperatorBasedStartDeployClusterTask.NAME
                ClusterProfileName.TERRAFORM.profileName -> {
                    when (val providerName = DeployClusterUtil.getTerraformProvider(project)) {
                        TerraformProviderName.AWS_EKS.providerName ->
                            TerraformBasedAwsEksStartDeployClusterTask.NAME
                        else -> {
                            throw NotSupportedException("Provided terraform provider name `$providerName` is not supported. Choose one of ${
                                TerraformProviderName.values().joinToString()
                            }")
                        }
                    }
                }
                else -> {
                    throw NotSupportedException("Provided profile name `$profileName` is not supported. Choose one of ${
                        ClusterProfileName.values().joinToString()
                    }")
                }
            }
        )

        this.dependsOn(dependencies)

        this.finalizedBy(RunCliTask.NAME)
    }
}
