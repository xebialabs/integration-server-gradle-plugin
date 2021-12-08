package ai.digital.integration.server.deploy.tasks.cluster.operator.awseks

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.cluster.operator.AwsEksHelper
import ai.digital.integration.server.deploy.tasks.cluster.operator.OperatorBasedStopTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedAwsEksDeployClusterStopTask : OperatorBasedStopTask() {

    companion object {
        const val NAME = "operatorBasedAwsEksStopDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.finalizedBy(finalizedBy())
    }

    @TaskAction
    fun launch() {
        AwsEksHelper(project).shutdownCluster()
    }
}
