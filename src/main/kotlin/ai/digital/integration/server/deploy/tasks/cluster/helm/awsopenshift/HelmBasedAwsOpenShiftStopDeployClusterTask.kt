package ai.digital.integration.server.deploy.tasks.cluster.helm.awsopenshift

import ai.digital.integration.server.common.cluster.helm.AwsOpenshiftHelmHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.tasks.cluster.helm.DeployHelmBasedStopTask
import org.gradle.api.tasks.TaskAction

open class HelmBasedAwsOpenShiftStopDeployClusterTask : DeployHelmBasedStopTask() {

    companion object {
        const val NAME = "helmBasedAwsOpenShiftStopDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        AwsOpenshiftHelmHelper(project, ProductName.DEPLOY).shutdownCluster()
    }
}
