package ai.digital.integration.server.tasks

import ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.domain.Server
import ai.digital.integration.server.tasks.cli.CopyCliBuildArtifactsTask
import ai.digital.integration.server.tasks.cli.RunCliTask
import ai.digital.integration.server.tasks.database.DatabaseStartTask
import ai.digital.integration.server.tasks.database.ImportDbUnitDataTask
import ai.digital.integration.server.tasks.database.PrepareDatabaseTask
import ai.digital.integration.server.tasks.mq.StartMqTask
import ai.digital.integration.server.tasks.provision.RunDatasetGenerationTask
import ai.digital.integration.server.tasks.provision.RunDevOpsAsCodeTask
import ai.digital.integration.server.tasks.satellite.StartSatelliteTask
import ai.digital.integration.server.tasks.worker.StartWorkersTask
import ai.digital.integration.server.util.DbUtil.Companion.isDerby
import ai.digital.integration.server.util.DeployServerUtil.Companion.getLogDir
import ai.digital.integration.server.util.DeployServerUtil.Companion.getResolvedDockerFile
import ai.digital.integration.server.util.DeployServerUtil.Companion.getServer
import ai.digital.integration.server.util.DeployServerUtil.Companion.getServerWorkingDir
import ai.digital.integration.server.util.DeployServerUtil.Companion.grantPermissionsToIntegrationServerFolder
import ai.digital.integration.server.util.DeployServerUtil.Companion.isAkkaSecured
import ai.digital.integration.server.util.DeployServerUtil.Companion.isDockerBased
import ai.digital.integration.server.util.DeployServerUtil.Companion.isTls
import ai.digital.integration.server.util.DeployServerUtil.Companion.startServerFromClasspath
import ai.digital.integration.server.util.DeployServerUtil.Companion.waitForBoot
import ai.digital.integration.server.util.EnvironmentUtil.Companion.getServerEnv
import ai.digital.integration.server.util.ProcessUtil.Companion.exec
import ai.digital.integration.server.util.SatelliteUtil.Companion.hasSatellites
import ai.digital.integration.server.util.ShutdownUtil.Companion.shutdownServer
import ai.digital.integration.server.util.WorkerUtil.Companion.hasWorkers
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.closureOf
import java.io.File
import java.nio.file.Paths

open class StartIntegrationServerTask : DefaultTask() {

    companion object {
        @JvmStatic
        val NAME = "startIntegrationServer"
    }

    init {
        group = PLUGIN_GROUP

        val dependencies = mutableListOf(ApplicationConfigurationOverrideTask.NAME, CentralConfigurationTask.NAME, CheckUILibVersionsTask.NAME, CopyCliBuildArtifactsTask.NAME, CopyServerBuildArtifactsTask.NAME, CopyOverlaysTask.NAME, if (isDerby(project)) "derbyStart" else DatabaseStartTask.NAME, DownloadAndExtractServerDistTask.NAME, PrepareDatabaseTask.NAME, PrepareDeployTask.NAME, SetLogbackLevelsTask.NAME, StartMqTask.NAME, YamlPatchTask.NAME)

        this.configure(closureOf<StartIntegrationServerTask> {

            if (isTls(project)) {
                dependencies.add(TlsApplicationConfigurationOverrideTask.NAME)
            }
            if (isAkkaSecured(project)) {
                dependencies.add(GenerateSecureAkkaKeysTask.NAME)
            }
            if (isAkkaSecured(project)) {
                dependencies.add(GenerateSecureAkkaKeysTask.NAME)
            }

            dependsOn(dependencies)

            if (!isDockerBased(project)) {
                if (hasWorkers(project)) {
                    finalizedBy(StartWorkersTask.NAME)
                }
                if (hasSatellites(project)) {
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
        return Paths.get(getServerWorkingDir(project), "bin").toFile()
    }

    private fun startServer(server: Server): Process {
        project.logger.lifecycle("Launching server")
        val environment: Map<String, String> = getServerEnv(project, server)
        project.logger.info("Starting server with environment: $environment")
        val map = mapOf(
                "command" to "run",
                "discardIO" to (server.stdoutFileName == null),
                "redirectTo" to if (server.stdoutFileName != null) File(getLogDir(project).toString() + "/" + server.stdoutFileName) else null,
                "environment" to environment,
                "params" to listOf("-force-upgrades"),
                "workDir" to getBinDir()
        )
        val process = exec(map)
        project.logger.lifecycle("Launched server on PID [" + process.pid().toString() + "] with command [" + process.info().commandLine().orElse("") + "].")
        return process
    }

    private fun hasToBeStartedFromClasspath(server: Server): Boolean {
        return server.runtimeDirectory != null
    }

    private fun start(server: Server): Process? {
        return if (!isDockerBased(project)) {
            maybeTearDown()
            if (hasToBeStartedFromClasspath(server)) {
                startServerFromClasspath(project)
            } else {
                startServer(server)
            }
        } else {
            project.exec {
                it.executable = "docker-compose"
                it.args = listOf("-f", getResolvedDockerFile(project).toFile().toString(), "up", "-d")
            }
            null
        }
    }

    private fun maybeTearDown() {
        shutdownServer(project)
    }

    private fun allowToWriteMountedHostFolders() {
        grantPermissionsToIntegrationServerFolder(project)
    }

    @TaskAction
    fun launch() {
        val server = getServer(project)
        project.logger.lifecycle("About to launch Deploy Server on port " + server.httpPort.toString() + ".")
        allowToWriteMountedHostFolders()
        val process = start(server)
        waitForBoot(project, process)
    }

}
