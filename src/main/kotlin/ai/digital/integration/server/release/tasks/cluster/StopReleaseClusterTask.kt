package ai.digital.integration.server.release.tasks.cluster

import ai.digital.integration.server.common.constant.ClusterProfileName
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import ai.digital.integration.server.release.tasks.cluster.operator.OperatorBasedStopReleaseClusterTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class StopReleaseClusterTask : DefaultTask() {

    companion object {
        const val NAME = "stopReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        this.dependsOn(
            when (val profileName = ReleaseClusterUtil.getProfile(project)) {
                ClusterProfileName.DOCKER_COMPOSE.profileName ->
                    throw IllegalArgumentException("Docker compose based cluster setup is not supported yet in Release.")
                ClusterProfileName.OPERATOR.profileName ->
                    OperatorBasedStopReleaseClusterTask.NAME
                ClusterProfileName.TERRAFORM.profileName -> {
                    throw IllegalArgumentException("Terraform based cluster setup is not supported yet in Release.")
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
