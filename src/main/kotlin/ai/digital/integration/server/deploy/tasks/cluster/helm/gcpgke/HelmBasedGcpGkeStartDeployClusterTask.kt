package ai.digital.integration.server.deploy.tasks.cluster.helm.gcpgke

import ai.digital.integration.server.common.cluster.helm.GcpGkeHelmHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.tasks.cluster.helm.DeployHelmBasedStartTask
import org.gradle.api.tasks.TaskAction

open class HelmBasedGcpGkeStartDeployClusterTask : DeployHelmBasedStartTask() {

    companion object {
        const val NAME = "helmBasedGcpGkeStartDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        GcpGkeHelmHelper(project, ProductName.DEPLOY).launchCluster()
        GcpGkeHelmHelper(project, ProductName.DEPLOY).setupHelmValues()
    }
}
