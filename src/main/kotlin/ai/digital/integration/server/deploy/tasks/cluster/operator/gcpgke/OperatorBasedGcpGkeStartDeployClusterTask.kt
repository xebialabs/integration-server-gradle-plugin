package ai.digital.integration.server.deploy.tasks.cluster.operator.gcpgke

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.cluster.operator.GcpGkeOperatorHelper
import ai.digital.integration.server.common.cluster.setup.GcpGkeHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.tasks.cluster.operator.DeployOperatorBasedStartTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedGcpGkeStartDeployClusterTask : DeployOperatorBasedStartTask() {

    companion object {
        const val NAME = "operatorBasedGcpGkeStartDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        GcpGkeHelper(project, ProductName.DEPLOY).launchCluster()
        GcpGkeOperatorHelper(project, ProductName.DEPLOY).updateOperator()
    }
}
