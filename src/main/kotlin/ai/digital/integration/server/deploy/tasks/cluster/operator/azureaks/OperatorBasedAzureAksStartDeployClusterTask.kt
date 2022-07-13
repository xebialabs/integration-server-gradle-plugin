package ai.digital.integration.server.deploy.tasks.cluster.operator.azureaks

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.cluster.operator.AzureAksOperatorHelper
import ai.digital.integration.server.common.cluster.setup.AzureAksHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.tasks.cluster.operator.DeployOperatorBasedStartTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAzureAksStartDeployClusterTask : DeployOperatorBasedStartTask() {

    companion object {
        const val NAME = "operatorBasedAzureAksStartDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        AzureAksOperatorHelper(project, ProductName.DEPLOY).launchCluster()
        AzureAksOperatorHelper(project, ProductName.DEPLOY).updateOperator()
    }
}
