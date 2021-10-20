package ai.digital.integration.server.deploy.tasks.server

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.mq.StartMqTask
import ai.digital.integration.server.common.tasks.database.DatabaseStartTask
import ai.digital.integration.server.common.tasks.database.ImportDbUnitDataTask
import ai.digital.integration.server.common.tasks.database.PrepareDatabaseTask
import ai.digital.integration.server.common.util.DbUtil
import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.deploy.tasks.cli.CopyCliBuildArtifactsTask
import ai.digital.integration.server.deploy.tasks.cli.RunCliTask
import ai.digital.integration.server.deploy.tasks.provision.RunDatasetGenerationTask
import ai.digital.integration.server.deploy.tasks.provision.RunDevOpsAsCodeTask
import ai.digital.integration.server.deploy.tasks.satellite.StartSatelliteTask
import ai.digital.integration.server.deploy.tasks.tls.GenerateSecureAkkaKeysTask
import ai.digital.integration.server.deploy.tasks.tls.TlsApplicationConfigurationOverrideTask
import ai.digital.integration.server.deploy.tasks.worker.StartWorkersTask
import ai.digital.integration.server.deploy.util.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.closureOf
import java.io.File
import java.nio.file.Paths

open class StartServerInstanceTask : DefaultTask() {
    companion object {
        const val NAME = "startServerInstanceTask"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        val dependencies = mutableListOf(
            ApplicationConfigurationOverrideTask.NAME,
            CentralConfigurationTask.NAME,
            CheckUILibVersionsTask.NAME,
            CopyCliBuildArtifactsTask.NAME,
            CopyServerBuildArtifactsTask.NAME,
            ServerCopyOverlaysTask.NAME, if (DbUtil.isDerby(project)) "derbyStart" else DatabaseStartTask.NAME,
            DownloadAndExtractServerDistTask.NAME,
            PrepareDatabaseTask.NAME,
            PrepareServerTask.NAME,
            SetServerLogbackLevelsTask.NAME,
            StartMqTask.NAME,
            ServerYamlPatchTask.NAME
        )

        this.configure(closureOf<StartServerInstanceTask> {

            if (DeployServerUtil.isTls(project)) {
                dependencies.add(TlsApplicationConfigurationOverrideTask.NAME)
            }
            if (DeployServerUtil.isAkkaSecured(project)) {
                dependencies.add(GenerateSecureAkkaKeysTask.NAME)
            }
            if (DeployServerUtil.isAkkaSecured(project)) {
                dependencies.add(GenerateSecureAkkaKeysTask.NAME)
            }

            dependsOn(dependencies)

            if (!DeployServerUtil.isDockerBased(project)) {
                if (WorkerUtil.hasWorkers(project)) {
                    finalizedBy(StartWorkersTask.NAME)
                }
                if (SatelliteUtil.hasSatellites(project)) {
                    finalizedBy(StartSatelliteTask.NAME)
                }
            }

            finalizedBy(ImportDbUnitDataTask.NAME)
            finalizedBy(RunDevOpsAsCodeTask.NAME)
            finalizedBy(RunDatasetGenerationTask.NAME)
            finalizedBy(RunCliTask.NAME)
        })
    }

    private fun getBinDir(): File? {
        return Paths.get(DeployServerUtil.getServerWorkingDir(project), "bin").toFile()
    }

    private fun startServer(server: Server): Process {
        project.logger.lifecycle("Launching server")
        val environment: Map<String, String> = EnvironmentUtil.getServerEnv(project, server)
        project.logger.info("Starting server with environment: $environment")
        val map = mapOf(
            "command" to "run",
            "discardIO" to (server.stdoutFileName == null),
            "redirectTo" to if (server.stdoutFileName != null) File(DeployServerUtil.getLogDir(project)
                .toString() + "/" + server.stdoutFileName) else null,
            "environment" to environment,
            "params" to listOf("-force-upgrades"),
            "workDir" to getBinDir()
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
                startServer(server)
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
        DeployServerUtil.grantPermissionsToIntegrationServerFolder(project)
    }

    @TaskAction
    fun launch() {
        val server = DeployServerUtil.getServer(project)
        project.logger.lifecycle("About to launch Deploy Server on port " + server.httpPort.toString() + ".")
        allowToWriteMountedHostFolders()
        val process = start(server)
        DeployServerUtil.waitForBoot(project, process)
    }
}
