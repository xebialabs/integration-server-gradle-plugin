package ai.digital.integration.server.release.tasks.cluster.operator.onprem

import ai.digital.integration.server.common.cluster.operator.OnPremHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedOnPremStopReleaseClusterTask : DefaultTask() {

    companion object {
        const val NAME = "operatorBasedOnPremStopReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        OnPremHelper(project, ProductName.RELEASE).shutdownCluster()
    }
}
