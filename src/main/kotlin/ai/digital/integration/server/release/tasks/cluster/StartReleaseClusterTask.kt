package ai.digital.integration.server.release.tasks.cluster

import ai.digital.integration.server.common.constant.ClusterProfileName
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import ai.digital.integration.server.deploy.tasks.cli.RunCliTask
import ai.digital.integration.server.release.tasks.cluster.operator.OperatorBasedStartReleaseClusterTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class StartReleaseClusterTask : DefaultTask() {

    companion object {
        const val NAME = "startReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        val dependencies = listOf(
            when (val profileName = ReleaseClusterUtil.getProfile(project)) {
                ClusterProfileName.DOCKER_COMPOSE.profileName ->
                    throw IllegalArgumentException("Docker compose based cluster setup is not supported yet in Release.")
                ClusterProfileName.OPERATOR.profileName ->
                    OperatorBasedStartReleaseClusterTask.NAME
                ClusterProfileName.TERRAFORM.profileName -> {
                    throw IllegalArgumentException("Terraform based cluster setup is not supported yet in Release.")
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
