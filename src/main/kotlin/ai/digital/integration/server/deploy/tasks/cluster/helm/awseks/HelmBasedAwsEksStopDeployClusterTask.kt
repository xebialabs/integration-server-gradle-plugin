package ai.digital.integration.server.deploy.tasks.cluster.helm.awseks

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.cluster.operator.AwsEksOperatorHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.tasks.cluster.helm.DeployHelmBasedStopTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.DeployOperatorBasedStopTask
import org.gradle.api.tasks.TaskAction

open class HelmBasedAwsEksStopDeployClusterTask : DeployHelmBasedStopTask() {

    companion object {
        const val NAME = "helmBasedAwsEksStopDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        //AwsEksOperatorHelper(project, ProductName.DEPLOY).shutdownCluster()
    }
}
