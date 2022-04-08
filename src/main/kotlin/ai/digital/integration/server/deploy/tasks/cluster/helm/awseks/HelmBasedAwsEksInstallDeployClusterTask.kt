package ai.digital.integration.server.deploy.tasks.cluster.helm.awseks

import ai.digital.integration.server.common.cluster.helm.AwsEksHelmHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.tasks.cluster.helm.DeployHelmBasedInstallTask
import org.gradle.api.tasks.TaskAction

open class HelmBasedAwsEksInstallDeployClusterTask : DeployHelmBasedInstallTask() {

    companion object {
        const val NAME = "helmBasedAwsEksInstallDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
       AwsEksHelmHelper(project, ProductName.DEPLOY).helmInstallCluster()
    }
}
