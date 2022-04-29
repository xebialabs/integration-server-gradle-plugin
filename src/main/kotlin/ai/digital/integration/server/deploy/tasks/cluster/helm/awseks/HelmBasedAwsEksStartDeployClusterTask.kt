package ai.digital.integration.server.deploy.tasks.cluster.helm.awseks

import ai.digital.integration.server.common.cluster.helm.AwsEksHelmHelper
import ai.digital.integration.server.common.cluster.setup.AwsEksHelper
import ai.digital.integration.server.common.cluster.operator.AwsEksOperatorHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.tasks.cluster.helm.DeployHelmBasedStartTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.DeployOperatorBasedStartTask
import org.gradle.api.tasks.TaskAction

open class HelmBasedAwsEksStartDeployClusterTask : DeployHelmBasedStartTask() {

    companion object {
        const val NAME = "helmBasedAwsEksStartDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        AwsEksHelmHelper(project, ProductName.DEPLOY).launchCluster()
        AwsEksHelmHelper(project, ProductName.DEPLOY).setupHelmValues()
    }
}
