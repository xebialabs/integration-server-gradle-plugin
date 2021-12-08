package ai.digital.integration.server.deploy.tasks.cluster.operator.onprem

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.cluster.operator.OnPremHelper
import ai.digital.integration.server.deploy.tasks.cluster.operator.OperatorBasedStopTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedOnPremStopDeployClusterTask : OperatorBasedStopTask() {

    companion object {
        const val NAME = "operatorBasedOnPremStopDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.finalizedBy(finalizedBy())
    }

    @TaskAction
    fun launch() {
        OnPremHelper(project).shutdownCluster()
    }
}
