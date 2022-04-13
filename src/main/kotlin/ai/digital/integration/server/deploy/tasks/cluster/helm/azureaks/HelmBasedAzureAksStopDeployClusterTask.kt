package ai.digital.integration.server.deploy.tasks.cluster.helm.azureaks

import ai.digital.integration.server.common.cluster.helm.AzureAksHelmHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.tasks.cluster.helm.DeployHelmBasedStopTask
import org.gradle.api.tasks.TaskAction

open class HelmBasedAzureAksStopDeployClusterTask : DeployHelmBasedStopTask() {

    companion object {
        const val NAME = "helmBasedAzureAksStopDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        AzureAksHelmHelper(project, ProductName.DEPLOY).shutdownCluster()
    }
}
