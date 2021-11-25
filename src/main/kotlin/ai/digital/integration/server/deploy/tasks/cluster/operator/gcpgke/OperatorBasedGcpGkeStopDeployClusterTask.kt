package ai.digital.integration.server.deploy.tasks.cluster.operator.gcpgke

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.cluster.operator.GcpGkeHelper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedGcpGkeStopDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "operatorBasedGcpGkeStopDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        GcpGkeHelper(project).shutdownCluster()
    }
}
