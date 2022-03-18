package ai.digital.integration.server.release.tasks.cluster.operator.azureaks

import ai.digital.integration.server.common.cluster.operator.AzureAksHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.release.tasks.cluster.operator.ReleaseOperatorBasedInstallTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAzureAksInstallReleaseClusterTask : ReleaseOperatorBasedInstallTask() {

    companion object {
        const val NAME = "operatorBasedAzureAksInstallReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        AzureAksHelper(project, ProductName.RELEASE).installCluster()
    }
}
