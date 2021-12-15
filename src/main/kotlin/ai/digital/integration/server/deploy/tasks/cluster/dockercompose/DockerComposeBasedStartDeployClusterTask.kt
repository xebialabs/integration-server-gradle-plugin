package ai.digital.integration.server.deploy.tasks.cluster.dockercompose

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.cluster.DeployDockerClusterHelper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class DockerComposeBasedStartDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "dockerComposeBasedStartDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        DeployDockerClusterHelper(project).launchCluster()
    }
}
