package ai.digital.integration.server.deploy.tasks.cluster

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.DockerClusterBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class DockerComposeBasedDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "dockerComposeBasedDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        DockerClusterBuilder(project).buildCluster()
    }
}
