package ai.digital.integration.server.release.tasks.cluster.helm.onprem

import ai.digital.integration.server.common.cluster.helm.OnPremHelmHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.release.tasks.cluster.helm.ReleaseHelmBasedStartTask
import org.gradle.api.tasks.TaskAction

open class HelmBasedOnPremStartReleaseClusterTask : ReleaseHelmBasedStartTask() {

    companion object {
        const val NAME = "helmBasedOnPremStartReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        OnPremHelmHelper(project, ProductName.RELEASE).launchCluster()
        OnPremHelmHelper(project, ProductName.RELEASE).setupHelmValues()
    }
}
