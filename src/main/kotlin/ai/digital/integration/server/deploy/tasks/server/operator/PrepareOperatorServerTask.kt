package ai.digital.integration.server.deploy.tasks.server.operator

import ai.digital.integration.server.common.cluster.util.OperatorUtil
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.DeployServerInitializeUtil
import ai.digital.integration.server.deploy.tasks.maintenance.CleanupBeforeStartupTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class PrepareOperatorServerTask : DefaultTask() {

    init {
        group = PluginConstant.PLUGIN_GROUP

        this.dependsOn(CleanupBeforeStartupTask.NAME)
    }

    @TaskAction
    fun launch() {
        val server = OperatorUtil(project).getOperatorServer()
        DeployServerInitializeUtil.prepare(project, server)
    }

    companion object {
        const val NAME = "prepareOperatorServer"
    }
}
