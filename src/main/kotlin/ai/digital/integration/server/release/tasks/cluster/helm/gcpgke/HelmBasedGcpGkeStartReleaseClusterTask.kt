package ai.digital.integration.server.release.tasks.cluster.helm.gcpgke

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.cluster.helm.GcpGkeHelmHelper
import ai.digital.integration.server.release.tasks.cluster.helm.ReleaseHelmBasedStartTask
import org.gradle.api.tasks.TaskAction

open class HelmBasedGcpGkeStartReleaseClusterTask : ReleaseHelmBasedStartTask() {

    companion object {
        const val NAME = "helmBasedGcpGkeStartReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        GcpGkeHelmHelper(project, ProductName.RELEASE).launchCluster()
        GcpGkeHelmHelper(project, ProductName.RELEASE).setupHelmValues()
    }
}
