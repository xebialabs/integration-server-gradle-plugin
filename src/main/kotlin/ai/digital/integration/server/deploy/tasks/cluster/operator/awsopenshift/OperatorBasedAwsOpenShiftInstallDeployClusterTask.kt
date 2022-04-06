package ai.digital.integration.server.deploy.tasks.cluster.operator.awsopenshift

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.cluster.operator.AwsOpenshiftOperatorHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.tasks.cluster.operator.DeployOperatorBasedInstallTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAwsOpenShiftInstallDeployClusterTask : DeployOperatorBasedInstallTask() {

    companion object {
        const val NAME = "operatorBasedAwsOpenShiftInstallDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        AwsOpenshiftOperatorHelper(project, ProductName.DEPLOY).installCluster()
    }
}
