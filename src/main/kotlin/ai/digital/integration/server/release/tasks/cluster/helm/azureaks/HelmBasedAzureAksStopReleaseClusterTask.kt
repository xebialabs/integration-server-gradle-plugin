package ai.digital.integration.server.release.tasks.cluster.helm.azureaks

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.cluster.helm.AzureAksHelmHelper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class HelmBasedAzureAksStopReleaseClusterTask : DefaultTask() {

    companion object {
        const val NAME = "helmBasedAzureAksStopReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        AzureAksHelmHelper(project, ProductName.RELEASE).shutdownCluster()
    }
}
