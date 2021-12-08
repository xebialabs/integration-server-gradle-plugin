package ai.digital.integration.server.deploy.tasks.cluster.operator.azureaks

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.cluster.operator.AzureAksHelper
import ai.digital.integration.server.deploy.tasks.cluster.operator.OperatorBasedStopTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAzureAksStopDeployClusterTask : OperatorBasedStopTask() {

    companion object {
        const val NAME = "operatorBasedAzureAksStopDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.finalizedBy(finalizedBy())
    }

    @TaskAction
    fun launch() {
        AzureAksHelper(project).shutdownCluster()
    }
}
