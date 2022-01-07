package ai.digital.integration.server.deploy.tasks.cluster.operator.onprem

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.cluster.operator.OnPremHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.tasks.cluster.operator.OperatorBasedStartTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedOnPremStartDeployClusterTask : OperatorBasedStartTask() {

    companion object {
        const val NAME = "operatorBasedOnPremStartDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        OnPremHelper(project, ProductName.DEPLOY).launchCluster()
    }
}
