package ai.digital.integration.server.deploy.tasks.cluster.helm.awsopenshift

import ai.digital.integration.server.common.cluster.helm.AwsOpenshiftHelmHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.tasks.cluster.helm.DeployHelmBasedStartTask
import org.gradle.api.tasks.TaskAction

open class HelmBasedAwsOpenShiftStartDeployClusterTask : DeployHelmBasedStartTask() {

    companion object {
        const val NAME = "helmBasedAwsOpenShiftStartDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        AwsOpenshiftHelmHelper(project, ProductName.DEPLOY).launchCluster()
        AwsOpenshiftHelmHelper(project, ProductName.DEPLOY).setupHelmValues()
    }
}
