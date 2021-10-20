package ai.digital.integration.server.deploy.tasks.cluster

import ai.digital.integration.server.common.constant.PluginConstant
import org.gradle.api.DefaultTask

open class StartDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "startDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        this.dependsOn(DockerComposeBasedDeployClusterTask.NAME)
    }
}
