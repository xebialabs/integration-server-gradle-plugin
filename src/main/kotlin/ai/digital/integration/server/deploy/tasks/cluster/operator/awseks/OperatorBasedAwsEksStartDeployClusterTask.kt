package ai.digital.integration.server.deploy.tasks.cluster.operator.awseks

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.cluster.operator.AwsEksHelper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAwsEksStartDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "operatorBasedAwsEksStartDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        AwsEksHelper(project).launchCluster()
    }
}
