package ai.digital.integration.server.deploy.tasks.cluster.helm.onprem

import ai.digital.integration.server.common.cluster.helm.OnPremHelmHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.cluster.operator.OnPremOperatorHelper
import ai.digital.integration.server.common.cluster.setup.OnPremHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.tasks.cluster.helm.DeployHelmBasedStartTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.DeployOperatorBasedStartTask
import org.gradle.api.tasks.TaskAction

open class HelmBasedOnPremStartDeployClusterTask : DeployHelmBasedStartTask() {

    companion object {
        const val NAME = "helmBasedOnPremStartDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        OnPremHelmHelper(project, ProductName.DEPLOY).launchCluster()
        OnPremHelmHelper(project, ProductName.DEPLOY).setupHelmValues()
    }
}
