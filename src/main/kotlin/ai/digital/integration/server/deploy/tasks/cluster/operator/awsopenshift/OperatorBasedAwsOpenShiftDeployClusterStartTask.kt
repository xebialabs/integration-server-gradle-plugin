package ai.digital.integration.server.deploy.tasks.cluster.operator.awsopenshift

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.cluster.operator.AwsOpenshiftHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.tasks.cluster.operator.OperatorBasedStartTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAwsOpenShiftDeployClusterStartTask : OperatorBasedStartTask() {

    companion object {
        const val NAME = "operatorBasedAwsOpenShiftStartDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        AwsOpenshiftHelper(project, ProductName.DEPLOY).launchCluster()
    }
}
