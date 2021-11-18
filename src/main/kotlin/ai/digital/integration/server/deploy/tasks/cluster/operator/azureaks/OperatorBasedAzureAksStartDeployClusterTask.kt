package ai.digital.integration.server.deploy.tasks.cluster.operator.azureaks

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.cluster.operator.AzureAksHelper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAzureAksStartDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "operatorBasedAzureAksStartDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        AzureAksHelper(project).launchCluster()
    }
}
