package ai.digital.integration.server.deploy.tasks.server

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.tasks.database.DatabaseStartTask
import ai.digital.integration.server.common.tasks.database.PrepareDatabaseTask
import ai.digital.integration.server.common.util.DbUtil
import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.internals.EnvironmentUtil
import ai.digital.integration.server.deploy.internals.ShutdownUtil
import ai.digital.integration.server.deploy.tasks.server.operator.OperatorCentralConfigurationTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.closureOf
import java.io.File
import java.nio.file.Paths

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

    private fun getBinDir(server: Server): File? {
        return Paths.get(DeployServerUtil.getServerWorkingDir(project, server), "bin").toFile()
    }

    private fun runWithPreviousInstallation(server: Server) {
        val previousServer = DeployServerUtil.getPreviousInstallationServer(project)
        val workDir = DeployServerUtil.getServerWorkingDir(project, previousServer)
        val logFile = Paths.get("${DeployServerUtil.getLogDir(project, server)}/deployit.log").toFile()

        project.logger.lifecycle("Initializing Deploy with previous installation from $workDir")

        val map = mapOf(
            "command" to "run",
            "environment" to EnvironmentUtil.getServerEnv(project, server),
            "params" to listOf("-setup", "-previous-installation", workDir, "-force-upgrades"),
            "workDir" to getBinDir(server)!!,
            "wait" to true
        )

        ProcessUtil.execAndCheck(map, logFile)
    }

    private fun startServer(server: Server): Process {
        project.logger.lifecycle("Launching server")
        val environment: Map<String, String> = EnvironmentUtil.getServerEnv(project, server)
        project.logger.info("Starting server with environment: $environment")
        val map = mapOf(
            "command" to "run",
            "discardIO" to (server.stdoutFileName == null),
            "redirectTo" to if (server.stdoutFileName != null) File(DeployServerUtil.getLogDir(project, server)
                .toString() + "/" + server.stdoutFileName) else null,
            "environment" to environment,
            "params" to listOf("-force-upgrades"),
            "workDir" to getBinDir(server)
        )
        val process = ProcessUtil.exec(map)
        project.logger.lifecycle("Launched server on PID [" + process.pid()
            .toString() + "] with command [" + process.info().commandLine().orElse("") + "].")
        return process
    }

    private fun hasToBeStartedFromClasspath(server: Server): Boolean {
        return server.runtimeDirectory != null
    }

    private fun start(server: Server): Process? {
        return if (!DeployServerUtil.isDockerBased(project)) {
            maybeTearDown()
            if (hasToBeStartedFromClasspath(server)) {
                DeployServerUtil.startServerFromClasspath(project)
            } else {
                if (DeployServerUtil.isPreviousInstallationServerDefined(project)) {
                    runWithPreviousInstallation(server)
                }
                startServer(server)
            }
        } else {
            project.exec {
                executable = "docker-compose"
                args = listOf("-f", DeployServerUtil.getResolvedDockerFile(project).toFile().toString(), "up", "-d")
            }
            null
        }
    }

    private fun maybeTearDown() {
        ShutdownUtil.shutdownServer(project)
    }

    private fun allowToWriteMountedHostFolders() {
        DeployServerUtil.grantPermissionsToIntegrationServerFolder(project)
    }

    @TaskAction
    fun launch() {
        DeployServerUtil.getServers(project)
            .filter { server -> !server.previousInstallation }
            .forEach { server ->
                project.logger.lifecycle("About to launch Deploy Server ${server.name} on port " + server.httpPort.toString() + ".")
                allowToWriteMountedHostFolders()
                val process = start(server)
                DeployServerUtil.waitForBoot(project, process, auxiliaryServer = true)
            }
    }
}
