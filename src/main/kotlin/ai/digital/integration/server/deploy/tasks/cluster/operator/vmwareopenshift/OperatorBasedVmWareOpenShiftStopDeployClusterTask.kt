package ai.digital.integration.server.deploy.tasks.cluster.operator.vmwareopenshift

import ai.digital.integration.server.common.cluster.operator.VmwareOpenshiftHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.tasks.cluster.operator.OperatorBasedStopTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedVmWareOpenShiftStopDeployClusterTask : OperatorBasedStopTask() {

    companion object {
        const val NAME = "operatorBasedVmWareOpenShiftStopDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        VmwareOpenshiftHelper(project, ProductName.DEPLOY).shutdownCluster()
    }
}
