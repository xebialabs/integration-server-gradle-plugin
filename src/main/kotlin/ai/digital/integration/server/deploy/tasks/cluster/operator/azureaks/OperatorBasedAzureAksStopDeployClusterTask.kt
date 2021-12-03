package ai.digital.integration.server.deploy.tasks.cluster.operator.azureaks

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.cluster.operator.AzureAksHelper
import ai.digital.integration.server.deploy.tasks.cli.DownloadAndExtractCliDistTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAzureAksStopDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "operatorBasedAzureAksStopDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(DownloadAndExtractCliDistTask.NAME)
    }

    @TaskAction
    fun launch() {
        AzureAksHelper(project).shutdownCluster()
    }
}
