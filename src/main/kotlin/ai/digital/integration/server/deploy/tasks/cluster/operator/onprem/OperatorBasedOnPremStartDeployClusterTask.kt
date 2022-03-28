package ai.digital.integration.server.deploy.tasks.cluster.operator.onprem

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.cluster.operator.OnPremHelper
import ai.digital.integration.server.common.cluster.setup.OnPrem
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.tasks.cluster.operator.DeployOperatorBasedStartTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedOnPremStartDeployClusterTask : DeployOperatorBasedStartTask() {

    companion object {
        const val NAME = "operatorBasedOnPremStartDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        OnPrem(project, ProductName.DEPLOY).launchCluster()
        OnPremHelper(project, ProductName.DEPLOY).updateOperator()
    }
}
