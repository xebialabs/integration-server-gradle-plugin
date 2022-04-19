package ai.digital.integration.server.deploy.tasks.cluster.helm.onprem

import ai.digital.integration.server.common.cluster.helm.OnPremHelmHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.cluster.operator.OnPremOperatorHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.tasks.cluster.helm.DeployHelmBasedInstallTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.DeployOperatorBasedInstallTask
import org.gradle.api.tasks.TaskAction

open class HelmBasedOnPremInstallDeployClusterTask : DeployHelmBasedInstallTask() {

    companion object {
        const val NAME = "helmBasedOnPremInstallDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        OnPremHelmHelper(project, ProductName.DEPLOY).helmInstallCluster()
    }
}
