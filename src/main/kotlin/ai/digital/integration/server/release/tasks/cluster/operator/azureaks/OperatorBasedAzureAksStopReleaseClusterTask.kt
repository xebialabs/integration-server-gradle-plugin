package ai.digital.integration.server.release.tasks.cluster.operator.azureaks

import ai.digital.integration.server.common.cluster.operator.AzureAksOperatorHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAzureAksStopReleaseClusterTask : DefaultTask() {

    companion object {
        const val NAME = "operatorBasedAzureAksStopReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        AzureAksOperatorHelper(project, ProductName.RELEASE).shutdownCluster()
    }
}
