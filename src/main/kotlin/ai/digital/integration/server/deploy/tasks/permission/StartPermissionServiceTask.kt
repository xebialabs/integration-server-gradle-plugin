package ai.digital.integration.server.deploy.tasks.permission

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.tasks.database.DatabaseStartTask
import ai.digital.integration.server.common.tasks.database.PrepareDatabaseTask
import ai.digital.integration.server.common.util.DbUtil
import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.deploy.domain.Permission
import ai.digital.integration.server.deploy.internals.*
import ai.digital.integration.server.deploy.tasks.server.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class StartPermissionServiceTask : DefaultTask() {
    companion object {
        const val NAME = "startPermissionService"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        val dependencies = mutableListOf(if (DbUtil.isDerby(project)) "derbyStart" else DatabaseStartTask.NAME,
                DownloadAndExtractPermissionDistTask.NAME,
                PreparePermissionServiceTask.NAME,
                PrepareDatabaseTask.NAME
        )
        this.dependsOn(dependencies)
    }

    private fun startServer(server: Permission): Process {
        project.logger.lifecycle("Launching permission service")
        project.logger.info("Starting in working dir: ${PermissionServiceUtil.getBinDir(project, server)}")
        val map = mapOf(
                "command" to "deploy-permission-service",
                "workDir" to PermissionServiceUtil.getBinDir(project, server),
                "params" to listOf("--server.port=${server.httpPort}"),
                "discardIO" to server.stdoutFileName.isNullOrEmpty(),
                "redirectTo" to if (!server.stdoutFileName.isNullOrEmpty()) File("${
                    PermissionServiceUtil.getPermissionServiceLogDir(project,
                            server)
                }/${server.stdoutFileName}") else null,
        )
        val process = ProcessUtil.exec(map)
        project.logger.lifecycle("Launched server on PID [" + process.pid().toString()
                + "] with command [" + process.info().commandLine().orElse("") + "].")
        return process
    }

    private fun start(server: Permission): Process? {
        return if (!PermissionServiceUtil.isDockerBased(project)) {
            maybeTearDown()
            val process = startServer(server)
            process
        } else {
            project.exec {
                it.executable = "docker-compose"
                it.args = listOf("-f", DeployServerUtil.getResolvedDockerFile(project).toFile().toString(), "up", "-d")
            }
            null
        }
    }

    private fun maybeTearDown() {
        ShutdownUtil.shutdownServer(project)
    }

    private fun allowToWriteMountedHostFolders() {
        PermissionServiceInitializeUtil.grantPermissionsToIntegrationServerFolder(project)
    }

    @TaskAction
    fun launch() {
        val server = PermissionServiceUtil.getPermissionService(project)
        project.logger.lifecycle("About to launch Permission Server on port " + server.stdoutFileName + ".")
        allowToWriteMountedHostFolders()
        val process = start(server)
        PermissionServiceInitializeUtil.waitForBoot(project, process)
    }
}
