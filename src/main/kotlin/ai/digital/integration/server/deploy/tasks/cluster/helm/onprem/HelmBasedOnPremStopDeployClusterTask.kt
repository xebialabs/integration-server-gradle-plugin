package ai.digital.integration.server.deploy.tasks.cluster.helm.onprem

import ai.digital.integration.server.common.cluster.helm.OnPremHelmHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.cluster.operator.OnPremOperatorHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.tasks.cluster.helm.DeployHelmBasedStopTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.DeployOperatorBasedStopTask
import org.gradle.api.tasks.TaskAction

open class HelmBasedOnPremStopDeployClusterTask : DeployHelmBasedStopTask() {

    companion object {
        const val NAME = "helmBasedOnPremStopDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        OnPremHelmHelper(project, ProductName.DEPLOY).shutdownCluster()
    }
}
