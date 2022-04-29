package ai.digital.integration.server.release.tasks.cluster.helm.azureaks

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.release.tasks.cluster.helm.ReleaseHelmBasedInstallTask
import ai.digital.integration.server.common.cluster.helm.AzureAksHelmHelper
import org.gradle.api.tasks.TaskAction

open class HelmBasedAzureAksInstallReleaseClusterTask : ReleaseHelmBasedInstallTask() {

    companion object {
        const val NAME = "helmBasedAzureAksInstallReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        AzureAksHelmHelper(project, ProductName.RELEASE).helmInstallCluster()
    }
}
