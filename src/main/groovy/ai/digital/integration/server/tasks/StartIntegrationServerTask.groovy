package ai.digital.integration.server.tasks

import ai.digital.integration.server.domain.Server
import ai.digital.integration.server.tasks.database.DatabaseStartTask
import ai.digital.integration.server.tasks.database.ImportDbUnitDataTask
import ai.digital.integration.server.tasks.database.PrepareDatabaseTask
import ai.digital.integration.server.tasks.mq.StartMqTask
import ai.digital.integration.server.tasks.provision.RunDatasetGenerationTask
import ai.digital.integration.server.tasks.provision.RunDevOpsAsCodeTask
import ai.digital.integration.server.tasks.satellite.StartSatelliteTask
import ai.digital.integration.server.tasks.worker.StartWorkersTask
import ai.digital.integration.server.util.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import static ai.digital.integration.server.util.ShutdownUtil.shutdownServer

class StartIntegrationServerTask extends DefaultTask {
    static NAME = "startIntegrationServer"

    StartIntegrationServerTask() {
        def dependencies = [
                CheckUILibVersionsTask.NAME,
                CopyOverlaysTask.NAME,
                DbUtil.isDerby(project) ? "derbyStart" : DatabaseStartTask.NAME,
                DownloadAndExtractServerDistTask.NAME,
                PrepareDatabaseTask.NAME,
                PrepareDeployTask.NAME,
                RemoveStdoutConfigTask.NAME,
                SetLogbackLevelsTask.NAME,
                StartMqTask.NAME,
                YamlPatchTask.NAME
        ]

        this.configure {
            group = PLUGIN_GROUP
            dependsOn(dependencies)

            if (!ServerUtil.isDockerBased(project)) {
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
        }
    }

    private def getBinDir() {
        Paths.get(ServerUtil.getServerWorkingDir(project), "bin").toFile()
    }

    private void initialize() {
        project.logger.lifecycle("Initializing Deploy")

        ProcessUtil.exec([
                command: "run",
                params : ["-setup", "-reinitialize", "-force", "-setup-defaults", "-force-upgrades", "conf/deployit.conf"],
                workDir: getBinDir(),
                wait   : true
        ])
    }

    private void startServer(Server server) {
        project.logger.lifecycle("Launching server")
        ProcessUtil.exec([
                command    : "run",
                params     : ["-force-upgrades"],
                environment: EnvironmentUtil.getServerEnv(server),
                workDir    : getBinDir(),
                inheritIO  : true
        ])
    }

    private static def hasToBeStartedFromClasspath(Server server) {
        server.runtimeDirectory != null
    }

    private def start(Server server) {
        if (!ServerUtil.isDockerBased(project)) {
            maybeTearDown()
            if (!hasToBeStartedFromClasspath(server)) {
                initialize()
            }
            if (hasToBeStartedFromClasspath(server)) {
                ServerUtil.startServerFromClasspath(project)
            } else {
                startServer(server)
            }
        } else {
            project.exec {
                it.executable "docker-compose"
                it.args '-f', ServerUtil.getResolvedDockerFile(project).toFile(), 'up', '-d'
            }
        }
    }

    private def maybeTearDown() {
        shutdownServer(project)
    }

    private def allowToWriteMountedHostFolders() {
        ServerUtil.grantPermissionsToIntegrationServerFolder(project)
    }

    @TaskAction
    void launch() {
        def server = ServerUtil.getServer(project)
        project.logger.lifecycle("About to launch Deploy Server on port ${server.httpPort}.")
        allowToWriteMountedHostFolders()

        start(server)
        ServerUtil.waitForBoot(project)
    }
}
