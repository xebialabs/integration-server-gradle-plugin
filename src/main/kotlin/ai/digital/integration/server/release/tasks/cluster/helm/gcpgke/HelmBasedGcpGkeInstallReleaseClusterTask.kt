package ai.digital.integration.server.release.tasks.cluster.helm.gcpgke

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.release.tasks.cluster.helm.ReleaseHelmBasedInstallTask
import ai.digital.integration.server.common.cluster.helm.GcpGkeHelmHelper
import org.gradle.api.tasks.TaskAction

open class HelmBasedGcpGkeInstallReleaseClusterTask : ReleaseHelmBasedInstallTask() {

    companion object {
        const val NAME = "helmBasedGcpGkeInstallReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        GcpGkeHelmHelper(project, ProductName.RELEASE).helmInstallCluster()
    }
}
