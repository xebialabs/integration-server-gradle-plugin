package ai.digital.integration.server.deploy.tasks.cluster.operator.vmwareopenshift

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.cluster.operator.VmwareOpenshiftHelper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedVmWareOpenShiftStopDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "operatorBasedVmWareOpenShiftStopDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        VmwareOpenshiftHelper(project).shutdownCluster()
    }
}
