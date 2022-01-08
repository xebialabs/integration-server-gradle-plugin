package ai.digital.integration.server.release.tasks.cluster.operator.gcpgke

import ai.digital.integration.server.common.cluster.operator.GcpGkeHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.release.tasks.cluster.operator.ReleaseOperatorBasedStopTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedGcpGkeStopReleaseClusterTask : ReleaseOperatorBasedStopTask() {

    companion object {
        const val NAME = "operatorBasedGcpGkeStopReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        GcpGkeHelper(project, ProductName.RELEASE).shutdownCluster()
    }
}
