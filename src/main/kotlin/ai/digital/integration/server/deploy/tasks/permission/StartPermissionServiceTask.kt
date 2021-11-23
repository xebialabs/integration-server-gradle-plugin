package ai.digital.integration.server.deploy.tasks.permission

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.mq.StartMqTask
import ai.digital.integration.server.common.tasks.database.DatabaseStartTask
import ai.digital.integration.server.common.tasks.database.PrepareDatabaseTask
import ai.digital.integration.server.common.util.DbUtil
import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.deploy.domain.Permission
import ai.digital.integration.server.deploy.internals.*
import ai.digital.integration.server.deploy.tasks.cli.CopyCliBuildArtifactsTask
import ai.digital.integration.server.deploy.tasks.cli.RunCliTask
import ai.digital.integration.server.deploy.tasks.satellite.DownloadAndExtractSatelliteDistTask
import ai.digital.integration.server.deploy.tasks.server.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.closureOf
import java.io.File
import java.nio.file.Paths

open class StartPermissionServiceTask : DefaultTask() {
    companion object {
        const val NAME = "startPermissionService"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(DownloadAndExtractPermissionDistTask.NAME)
        this.dependsOn(PreparePermissionServiceTask.NAME)
        this.dependsOn(PrepareDatabaseTask.NAME)
    }

    private fun getBinDir(): File? {
        return Paths.get(PermissionServiceUtil.getPermissionServiceWorkingDir(project), "bin").toFile()
    }

    private fun startServer(server: Permission): Process {
        project.logger.lifecycle("Launching permission service")
        val environment: Map<String, String> = EnvironmentUtil.getPermissionServiceEnv(project, server)
        project.logger.info("Starting permission server with environment: $environment")
        project.logger.info("Starting ${PermissionServiceUtil.getBinDir(project, server)}")
        val map = mapOf(
                "command" to "deploy-permission-service",
                "workDir" to PermissionServiceUtil.getBinDir(project, server),
                "environment" to environment,
                "discardIO" to server.stdoutFileName.isNullOrEmpty(),
                "redirectTo" to if (!server.stdoutFileName.isNullOrEmpty()) File("${
                    PermissionServiceUtil.getPermissionServiceLogDir(project,
                            server)
                }/${server.stdoutFileName}") else null,
        )
        val process = ProcessUtil.exec(map)
        project.exec{
            it.executable = "ls"
        }
        project.logger.lifecycle("Launched server on PID [" + process.pid()
                .toString() + "] with command [" + process.info().commandLine().orElse("") + "].")
        return process
    }

    private fun hasToBeStartedFromClasspath(server: Permission): Boolean {
        return server.runtimeDirectory != null
    }

    private fun start(server: Permission): Process? {
        return if (!PermissionServiceUtil.isDockerBased(project)) {
            maybeTearDown()
            if (false) {
                PermissionServiceInitializeUtil.startServerFromClasspath(project)
            } else {
                val process = startServer(server)
                project.logger.lifecycle("I'm HEREEEEEE $process")
                process
            }
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
