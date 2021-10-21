package ai.digital.integration.server.deploy.tasks.cluster

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.tasks.cluster.dockercompose.DockerComposeBasedStopDeployClusterTask
import org.gradle.api.DefaultTask

open class StopDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "stopDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        this.dependsOn(DockerComposeBasedStopDeployClusterTask.NAME)
    }
}
