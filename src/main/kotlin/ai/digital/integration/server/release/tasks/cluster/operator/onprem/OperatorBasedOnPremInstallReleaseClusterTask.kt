package ai.digital.integration.server.release.tasks.cluster.operator.onprem

import ai.digital.integration.server.common.cluster.operator.OnPremOperatorHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.release.tasks.cluster.operator.ReleaseOperatorBasedInstallTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedOnPremInstallReleaseClusterTask : ReleaseOperatorBasedInstallTask() {

    companion object {
        const val NAME = "operatorBasedOnPremInstallReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        OnPremOperatorHelper(project, ProductName.RELEASE).installCluster()
    }
}
