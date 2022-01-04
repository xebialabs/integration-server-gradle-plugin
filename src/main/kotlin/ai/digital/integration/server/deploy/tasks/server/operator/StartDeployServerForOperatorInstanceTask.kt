package ai.digital.integration.server.deploy.tasks.server.operator

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.tasks.database.DatabaseStartTask
import ai.digital.integration.server.common.tasks.database.PrepareDatabaseTask
import ai.digital.integration.server.common.util.DbUtil
import ai.digital.integration.server.common.util.DockerComposeUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.internals.cluster.operator.OperatorHelper
import ai.digital.integration.server.deploy.tasks.server.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.closureOf

open class StartDeployServerForOperatorInstanceTask : DefaultTask() {
    companion object {
        const val NAME = "startDeployServerForOperatorInstance"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        val dependencies = mutableListOf(
            ApplicationConfigurationOverrideTask.NAME,
            CopyServerFoldersTask.NAME,
            CopyServerBuildArtifactsTask.NAME,
            ServerCopyOverlaysTask.NAME, if (DbUtil.isDerby(project)) "derbyStart" else DatabaseStartTask.NAME,
            OperatorCentralConfigurationTask.NAME,
            PrepareDatabaseTask.NAME,
            PrepareServerTask.NAME,
            SetServerLogbackLevelsTask.NAME,
            ServerYamlPatchTask.NAME
        )

        this.configure(closureOf<StartDeployServerForOperatorInstanceTask> {
            dependsOn(dependencies)
        })
    }

    private fun start(server: Server) {
        DeployServerUtil.runDockerBasedInstance(project, server)
    }

    private fun allowToWriteMountedHostFolders() {
        DeployServerUtil.grantPermissionsToIntegrationServerFolder(project)
    }

    @TaskAction
    fun launch() {
        // we only need one server for deployment on the operators
        val operatorHelper = OperatorHelper.getOperatorHelper(project)
        val server = operatorHelper.getOperatorServer(project)
        if (!server.previousInstallation) {
            project.logger.lifecycle("About to launch Deploy Server ${server.name} on port " + server.httpPort.toString() + ".")
            allowToWriteMountedHostFolders()
            start(server)
            DeployServerUtil.waitForBoot(project, null, server, auxiliaryServer = true)

            val dockerComposeFile = DeployServerUtil.getResolvedDockerFile(project, server).toFile()
            DockerComposeUtil.allowToCleanMountedFiles(project, server, dockerComposeFile)
        }
    }
}
