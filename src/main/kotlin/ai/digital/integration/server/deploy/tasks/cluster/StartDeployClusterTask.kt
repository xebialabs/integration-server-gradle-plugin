package ai.digital.integration.server.deploy.tasks.cluster

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.tasks.cli.RunCliTask
import ai.digital.integration.server.deploy.tasks.cluster.dockercompose.DockerComposeBasedStartDeployClusterTask
import org.gradle.api.DefaultTask

open class StartDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "startDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        val dependencies = listOf(
            DockerComposeBasedStartDeployClusterTask.NAME
        )

        this.dependsOn(dependencies)

        this.finalizedBy(RunCliTask.NAME)
    }
}
