package ai.digital.integration.server.release.tasks.cluster.operator.vmwareopenshift

import ai.digital.integration.server.common.cluster.operator.VmwareOpenshiftHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedVmWareOpenShiftStopReleaseClusterTask : DefaultTask() {

    companion object {
        const val NAME = "operatorBasedVmWareOpenShiftStopReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        VmwareOpenshiftHelper(project, ProductName.RELEASE).shutdownCluster()
    }
}
