package ai.digital.integration.server.deploy.tasks.cluster

import ai.digital.integration.server.common.constant.ClusterProfileName
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.TerraformProviderName
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import ai.digital.integration.server.deploy.tasks.cluster.dockercompose.DockerComposeBasedStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.OperatorBasedStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.terraform.TerraformBasedAwsEksStopDeployClusterTask
import org.gradle.api.DefaultTask
import org.gradle.internal.impldep.org.eclipse.jgit.errors.NotSupportedException

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
                ClusterProfileName.OPERATOR.profileName ->
                    OperatorBasedStopDeployClusterTask.NAME
                ClusterProfileName.TERRAFORM.profileName -> {
                    when (val providerName = DeployClusterUtil.getTerraformProvider(project)) {
                        TerraformProviderName.AWS_EKS.providerName ->
                            TerraformBasedAwsEksStopDeployClusterTask.NAME
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
            })
    }
}
