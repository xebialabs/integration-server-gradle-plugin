package ai.digital.integration.server.deploy.tasks.cluster.terraform

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.util.TerraformHelper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class TerraformBasedAwsEksStopDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "terraformBasedAwsEksStopDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        TerraformHelper(project).shutdownCluster()
    }
}
