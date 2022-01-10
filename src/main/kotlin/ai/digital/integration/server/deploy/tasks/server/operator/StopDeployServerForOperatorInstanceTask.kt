package ai.digital.integration.server.deploy.tasks.server.operator

import ai.digital.integration.server.common.cluster.operator.OperatorHelper
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.util.DockerComposeUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class StopDeployServerForOperatorInstanceTask : DefaultTask() {
    companion object {
        const val NAME = "stopDeployServerForOperatorInstance"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        val operatorHelper = OperatorHelper.getOperatorHelper(project, ProductName.DEPLOY)
        val server = operatorHelper.getOperatorDeployServer(project)
        val dockerComposeFile = DeployServerUtil.getResolvedDockerFile(project, server).toFile()
        DockerComposeUtil.allowToCleanMountedFiles(project, server, dockerComposeFile)

        val args = listOf(
            "-f",
            dockerComposeFile.toString(),
            "down"
        )
        DockerComposeUtil.execute(project, args)
    }
}
