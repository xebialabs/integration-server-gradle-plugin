package ai.digital.integration.server.release.tasks.cluster.helm.onprem

import ai.digital.integration.server.common.cluster.helm.OnPremHelmHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.release.tasks.cluster.helm.ReleaseHelmBasedInstallTask
import org.gradle.api.tasks.TaskAction

open class HelmBasedOnPremInstallReleaseClusterTask : ReleaseHelmBasedInstallTask() {

    companion object {
        const val NAME = "helmBasedOnPremInstallReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        OnPremHelmHelper(project, ProductName.RELEASE).helmInstallCluster()
    }
}
