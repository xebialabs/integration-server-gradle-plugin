package ai.digital.integration.server.release.tasks.cluster.helm.gcpgke

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.cluster.helm.GcpGkeHelmHelper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class HelmBasedGcpGkeStopReleaseClusterTask : DefaultTask() {

    companion object {
        const val NAME = "helmBasedGcpGkeStopReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        GcpGkeHelmHelper(project, ProductName.RELEASE).shutdownCluster()
    }
}
