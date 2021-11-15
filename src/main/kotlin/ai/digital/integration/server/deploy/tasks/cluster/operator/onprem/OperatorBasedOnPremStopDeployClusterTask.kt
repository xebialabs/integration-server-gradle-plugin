package ai.digital.integration.server.deploy.tasks.cluster.operator.onprem

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.cluster.operator.OnPremHelper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedOnPremStopDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "operatorBasedOnPremStopDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        OnPremHelper(project).shutdownCluster()
    }
}