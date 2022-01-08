package ai.digital.integration.server.deploy.tasks.cluster.operator.awsopenshift

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.cluster.operator.AwsOpenshiftHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.tasks.cluster.operator.DeployOperatorBasedStopTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAwsOpenShiftDeployClusterStopTask : DeployOperatorBasedStopTask() {

    companion object {
        const val NAME = "operatorBasedAwsOpenShiftDeployClusterStop"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        AwsOpenshiftHelper(project, ProductName.DEPLOY).shutdownCluster()
    }
}
