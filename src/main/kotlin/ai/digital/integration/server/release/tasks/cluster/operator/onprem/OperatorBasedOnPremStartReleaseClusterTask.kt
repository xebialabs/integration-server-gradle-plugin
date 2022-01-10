package ai.digital.integration.server.release.tasks.cluster.operator.onprem

import ai.digital.integration.server.common.cluster.operator.OnPremHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.release.tasks.cluster.operator.ReleaseOperatorBasedStartTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedOnPremStartReleaseClusterTask : ReleaseOperatorBasedStartTask() {

    companion object {
        const val NAME = "operatorBasedOnPremStartReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        OnPremHelper(project, ProductName.RELEASE).launchCluster()
    }
}
