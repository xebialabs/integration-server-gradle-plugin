package ai.digital.integration.server.deploy.tasks.server.operator

import ai.digital.integration.server.common.cluster.operator.OperatorHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.internals.DeployServerInitializeUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
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
        val operatorHelper = OperatorHelper.getOperatorHelper(project, ProductName.DEPLOY)
        val server = operatorHelper.getOperatorDeployServer(project)
        server.httpPort = 4516 // we need default port in the image
        DeployServerInitializeUtil.prepare(project, server)
    }

    companion object {
        const val NAME = "prepareOperatorServer"
    }
}
