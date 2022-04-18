package ai.digital.integration.server.deploy.tasks.cluster.helm.gcpgke

import ai.digital.integration.server.common.cluster.helm.GcpGkeHelmHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.tasks.cluster.helm.DeployHelmBasedStopTask
import org.gradle.api.tasks.TaskAction

open class HelmBasedGcpGkeStopDeployClusterTask : DeployHelmBasedStopTask() {

    companion object {
        const val NAME = "helmBasedGcpGkeStopDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        GcpGkeHelmHelper(project, ProductName.DEPLOY).shutdownCluster()
    }
}
