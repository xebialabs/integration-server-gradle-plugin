package ai.digital.integration.server.release.tasks.cluster.operator.vmwareopenshift

import ai.digital.integration.server.common.cluster.operator.VmwareOpenshiftHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.release.tasks.cluster.operator.ReleaseOperatorBasedStopTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedVmWareOpenShiftStopReleaseClusterTask : ReleaseOperatorBasedStopTask() {

    companion object {
        const val NAME = "operatorBasedVmWareOpenShiftStopReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        VmwareOpenshiftHelper(project, ProductName.RELEASE).shutdownCluster()
    }
}
