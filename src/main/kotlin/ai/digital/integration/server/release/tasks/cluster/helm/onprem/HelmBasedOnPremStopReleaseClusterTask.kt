package ai.digital.integration.server.release.tasks.cluster.helm.onprem

import ai.digital.integration.server.common.cluster.helm.OnPremHelmHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class HelmBasedOnPremStopReleaseClusterTask : DefaultTask() {

    companion object {
        const val NAME = "helmBasedOnPremStopReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        OnPremHelmHelper(project, ProductName.RELEASE).shutdownCluster()
    }
}
