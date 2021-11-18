package ai.digital.integration.server.deploy.tasks.cluster.terraform

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.cluster.DeployOperatorClusterHelper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class TerraformBasedAwsEksStartDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "terraformBasedAwsEksStartDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        DeployOperatorClusterHelper(project).launchCluster()
    }
}
