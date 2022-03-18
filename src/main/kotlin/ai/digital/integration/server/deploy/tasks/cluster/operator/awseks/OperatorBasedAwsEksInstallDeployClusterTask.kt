package ai.digital.integration.server.deploy.tasks.cluster.operator.awseks

import ai.digital.integration.server.common.cluster.operator.AwsEksHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.tasks.cluster.operator.DeployOperatorBasedInstallTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAwsEksInstallDeployClusterTask : DeployOperatorBasedInstallTask() {

    companion object {
        const val NAME = "operatorBasedAwsEksInstallDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        AwsEksHelper(project, ProductName.DEPLOY).installCluster()
    }
}
