package ai.digital.integration.server.deploy.tasks.cluster.operator.awseks

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.cluster.operator.AwsEksHelper
import ai.digital.integration.server.deploy.tasks.cluster.operator.OperatorBasedStartTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAwsEksDeployClusterStartTask : OperatorBasedStartTask() {

    companion object {
        const val NAME = "operatorBasedAwsEksStartDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        AwsEksHelper(project).launchCluster()
    }
}
