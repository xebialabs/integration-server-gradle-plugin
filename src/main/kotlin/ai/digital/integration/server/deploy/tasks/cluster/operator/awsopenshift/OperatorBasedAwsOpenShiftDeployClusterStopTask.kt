package ai.digital.integration.server.deploy.tasks.cluster.operator.awsopenshift

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.cluster.operator.AwsOpenshiftHelper
import ai.digital.integration.server.deploy.tasks.cluster.operator.OperatorBasedStopTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAwsOpenShiftDeployClusterStopTask : OperatorBasedStopTask() {

    companion object {
        const val NAME = "operatorBasedAwsOpenShiftDeployClusterStop"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        AwsOpenshiftHelper(project).shutdownCluster()
    }
}
