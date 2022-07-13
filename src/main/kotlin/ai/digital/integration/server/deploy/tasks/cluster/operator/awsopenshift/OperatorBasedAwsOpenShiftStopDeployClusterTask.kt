package ai.digital.integration.server.deploy.tasks.cluster.operator.awsopenshift

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.cluster.operator.AwsOpenshiftOperatorHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.tasks.cluster.operator.DeployOperatorBasedStopTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAwsOpenShiftStopDeployClusterTask : DeployOperatorBasedStopTask() {

    companion object {
        const val NAME = "operatorBasedAwsOpenShiftStopDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        AwsOpenshiftOperatorHelper(project, ProductName.DEPLOY).shutdownCluster()
    }
}
