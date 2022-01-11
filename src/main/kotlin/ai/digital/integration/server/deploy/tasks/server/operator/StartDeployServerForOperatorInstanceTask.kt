package ai.digital.integration.server.deploy.tasks.server.operator

import ai.digital.integration.server.common.cluster.util.OperatorUtil
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.Server

import ai.digital.integration.server.common.util.DockerComposeUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.tasks.server.ApplicationConfigurationOverrideTask

import ai.digital.integration.server.deploy.tasks.server.ServerCopyOverlaysTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.closureOf

open class StartDeployServerForOperatorInstanceTask : DefaultTask() {
    companion object {
        const val NAME = "startDeployServerForOperatorInstance"
    }

    private val clusterUtil = OperatorUtil(project)

    init {
        group = PluginConstant.PLUGIN_GROUP

        val dependencies = mutableListOf(
            ApplicationConfigurationOverrideTask.NAME,
            OperatorCentralConfigurationTask.NAME,
            PrepareOperatorServerTask.NAME,
            ServerCopyOverlaysTask.NAME
        )

        this.configure(closureOf<StartDeployServerForOperatorInstanceTask> {
            dependsOn(dependencies)
        })
    }

    private fun start(server: Server) {
        DeployServerUtil.runDockerBasedInstance(project, server)
    }

    private fun allowToWriteMountedHostFolders() {
        clusterUtil.grantPermissionsToIntegrationServerFolder()
    }

    @TaskAction
    fun launch() {
        // we only need one server for deployment on the operators
        val server = clusterUtil.getOperatorServer()
        project.logger.lifecycle("About to launch Deploy Server ${server.name} on port " + server.httpPort.toString() + ".")

        allowToWriteMountedHostFolders()
        start(server)
        OperatorUtil(project).waitForBoot(server)

        val dockerComposeFile = DeployServerUtil.getResolvedDockerFile(project, server).toFile()
        DockerComposeUtil.allowToCleanMountedFiles(project, server, dockerComposeFile)
    }
}
