package ai.digital.integration.server.deploy.tasks.cluster

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.util.ClusterUtil.Companion.createNetwork
import ai.digital.integration.server.deploy.util.ClusterUtil.Companion.inspectIps
import ai.digital.integration.server.deploy.util.ClusterUtil.Companion.runServers
import ai.digital.integration.server.deploy.util.ClusterUtil.Companion.runWorkers
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
        createNetwork(project)
        runServers(project)
        inspectIps(project)
        runWorkers(project)
    }
}
