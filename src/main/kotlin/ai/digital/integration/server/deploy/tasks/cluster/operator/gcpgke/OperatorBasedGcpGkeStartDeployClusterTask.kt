package ai.digital.integration.server.deploy.tasks.cluster.operator.gcpgke

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.cluster.operator.GcpGkeHelper
import ai.digital.integration.server.deploy.tasks.cluster.operator.OperatorBasedStartTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedGcpGkeStartDeployClusterTask : OperatorBasedStartTask() {

    companion object {
        const val NAME = "operatorBasedGcpGkeStartDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(dependsOnTasks())
    }

    @TaskAction
    fun launch() {
        GcpGkeHelper(project).launchCluster()
    }
}
