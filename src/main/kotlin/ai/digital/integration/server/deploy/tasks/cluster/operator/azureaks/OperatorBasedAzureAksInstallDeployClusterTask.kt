package ai.digital.integration.server.deploy.tasks.cluster.operator.azureaks

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.cluster.operator.AzureAksHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.tasks.cluster.operator.DeployOperatorBasedInstallTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAzureAksInstallDeployClusterTask : DeployOperatorBasedInstallTask() {

    companion object {
        const val NAME = "operatorBasedAzureAksInstallDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        AzureAksHelper(project, ProductName.DEPLOY).installCluster()
    }
}
