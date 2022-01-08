package ai.digital.integration.server.release.tasks.cluster.operator.azureaks

import ai.digital.integration.server.common.cluster.operator.AzureAksHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.release.tasks.cluster.operator.ReleaseOperatorBasedStopTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAzureAksStopReleaseClusterTask : ReleaseOperatorBasedStopTask() {

    companion object {
        const val NAME = "operatorBasedAzureAksStopReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        AzureAksHelper(project, ProductName.RELEASE).shutdownCluster()
    }
}
