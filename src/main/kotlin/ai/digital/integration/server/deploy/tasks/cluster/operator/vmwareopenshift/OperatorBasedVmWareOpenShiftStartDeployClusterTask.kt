package ai.digital.integration.server.deploy.tasks.cluster.operator.vmwareopenshift

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.cluster.operator.VmwareOpenshiftHelper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedVmWareOpenShiftStartDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "operatorBasedVmWareOpenShiftStartDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        VmwareOpenshiftHelper(project).launchCluster()
    }
}
