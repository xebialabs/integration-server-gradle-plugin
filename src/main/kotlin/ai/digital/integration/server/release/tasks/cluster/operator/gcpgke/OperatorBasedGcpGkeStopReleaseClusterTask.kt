package ai.digital.integration.server.release.tasks.cluster.operator.gcpgke

import ai.digital.integration.server.common.cluster.operator.GcpGkeOperatorHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedGcpGkeStopReleaseClusterTask : DefaultTask() {

    companion object {
        const val NAME = "operatorBasedGcpGkeStopReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        GcpGkeOperatorHelper(project, ProductName.RELEASE).shutdownCluster()
    }
}
