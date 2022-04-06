package ai.digital.integration.server.deploy.tasks.cluster.operator.azureaks

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.cluster.operator.AzureAksOperatorHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.tasks.cluster.operator.DeployOperatorBasedStopTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAzureAksStopDeployClusterTask : DeployOperatorBasedStopTask() {

    companion object {
        const val NAME = "operatorBasedAzureAksStopDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        AzureAksOperatorHelper(project, ProductName.DEPLOY).shutdownCluster()
    }
}
