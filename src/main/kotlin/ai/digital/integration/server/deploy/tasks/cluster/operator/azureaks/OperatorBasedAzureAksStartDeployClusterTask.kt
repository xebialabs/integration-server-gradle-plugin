package ai.digital.integration.server.deploy.tasks.cluster.operator.azureaks

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.cluster.operator.AzureAksHelper
import ai.digital.integration.server.deploy.tasks.cli.DownloadAndExtractCliDistTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.OperatorBasedStartTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAzureAksStartDeployClusterTask : OperatorBasedStartTask() {

    companion object {
        const val NAME = "operatorBasedAzureAksStartDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        AzureAksHelper(project).launchCluster()
    }
}
