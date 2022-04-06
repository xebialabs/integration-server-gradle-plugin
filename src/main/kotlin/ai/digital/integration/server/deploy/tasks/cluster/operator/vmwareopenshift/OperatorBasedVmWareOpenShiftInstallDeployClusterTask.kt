package ai.digital.integration.server.deploy.tasks.cluster.operator.vmwareopenshift

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.cluster.operator.VmwareOpenshiftOperatorHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.tasks.cluster.operator.DeployOperatorBasedInstallTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedVmWareOpenShiftInstallDeployClusterTask : DeployOperatorBasedInstallTask() {

    companion object {
        const val NAME = "operatorBasedVmWareOpenShiftInstallDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        VmwareOpenshiftOperatorHelper(project, ProductName.DEPLOY).installCluster()
    }
}
