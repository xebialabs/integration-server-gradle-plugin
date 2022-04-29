package ai.digital.integration.server.deploy.tasks.cluster.helm.gcpgke

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.tasks.cluster.helm.DeployHelmBasedInstallTask
import ai.digital.integration.server.common.cluster.helm.GcpGkeHelmHelper
import org.gradle.api.tasks.TaskAction

open class HelmBasedGcpGkeInstallDeployClusterTask : DeployHelmBasedInstallTask() {

    companion object {
        const val NAME = "helmBasedGcpGkeInstallDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        GcpGkeHelmHelper(project, ProductName.DEPLOY).helmInstallCluster()
    }
}
