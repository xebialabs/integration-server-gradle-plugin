package ai.digital.integration.server

import ai.digital.integration.server.tasks.*
import ai.digital.integration.server.tasks.anonymizer.ExportDatabaseTask
import ai.digital.integration.server.tasks.cli.CliCleanDefaultExtTask
import ai.digital.integration.server.tasks.cli.CliOverlaysTask
import ai.digital.integration.server.tasks.cli.CopyCliBuildArtifactsTask
import ai.digital.integration.server.tasks.cli.DownloadAndExtractCliDistTask
import ai.digital.integration.server.tasks.cli.RunCliTask
import ai.digital.integration.server.tasks.database.DatabaseStartTask
import ai.digital.integration.server.tasks.database.DatabaseStopTask
import ai.digital.integration.server.tasks.database.ImportDbUnitDataTask
import ai.digital.integration.server.tasks.database.PrepareDatabaseTask
import ai.digital.integration.server.tasks.gitlab.GitlabStartTask
import ai.digital.integration.server.tasks.gitlab.GitlabStopTask
import ai.digital.integration.server.tasks.mq.ShutdownMqTask
import ai.digital.integration.server.tasks.mq.StartMqTask
import ai.digital.integration.server.tasks.pluginManager.StartPluginManagerTask
import ai.digital.integration.server.tasks.provision.RunDatasetGenerationTask
import ai.digital.integration.server.tasks.provision.RunDevOpsAsCodeTask
import ai.digital.integration.server.tasks.satellite.DownloadAndExtractSatelliteDistTask
import ai.digital.integration.server.tasks.satellite.PrepareSatellitesTask
import ai.digital.integration.server.tasks.satellite.SatelliteOverlaysTask
import ai.digital.integration.server.tasks.satellite.ShutdownSatelliteTask
import ai.digital.integration.server.tasks.satellite.StartSatelliteTask
import ai.digital.integration.server.tasks.worker.CopyIntegrationServerTask
import ai.digital.integration.server.tasks.worker.SyncServerPluginsWithWorkerTask
import ai.digital.integration.server.tasks.worker.DownloadAndExtractWorkerDistTask
import ai.digital.integration.server.tasks.worker.SetWorkersLogbackLevelsTask
import ai.digital.integration.server.tasks.worker.ShutdownWorkersTask
import ai.digital.integration.server.tasks.worker.StartWorkersTask
import ai.digital.integration.server.tasks.worker.WorkerOverlaysTask
import ai.digital.integration.server.util.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

class IntegrationServerPlugin implements Plugin<Project> {

    private static void createTasks(Project project, Configuration itcfg) {

        //CLI
        project.tasks.create(CliCleanDefaultExtTask.NAME, CliCleanDefaultExtTask)
        project.tasks.create(CopyCliBuildArtifactsTask.NAME, CopyCliBuildArtifactsTask)
        project.tasks.create(CliOverlaysTask.NAME, CliOverlaysTask)
        project.tasks.create(DownloadAndExtractCliDistTask.NAME, DownloadAndExtractCliDistTask)
        project.tasks.create(RunCliTask.NAME, RunCliTask)

        //Database
        project.tasks.create(DatabaseStartTask.NAME, DatabaseStartTask)
        project.tasks.create(DatabaseStopTask.NAME, DatabaseStopTask)

        //Deploy Server
        project.tasks.create(ApplicationConfigurationOverrideTask.NAME, ApplicationConfigurationOverrideTask)
        project.tasks.create(CentralConfigurationTask.NAME, CentralConfigurationTask)
        project.tasks.create(CheckUILibVersionsTask.NAME, CheckUILibVersionsTask)
        project.tasks.create(CopyServerBuildArtifactsTask.NAME, CopyServerBuildArtifactsTask)
        project.tasks.create(CopyOverlaysTask.NAME, CopyOverlaysTask)
        project.tasks.create(DockerBasedStopDeployTask.NAME, DockerBasedStopDeployTask)
        project.tasks.create(DownloadAndExtractDbUnitDataDistTask.NAME, DownloadAndExtractDbUnitDataDistTask)
        project.tasks.create(DownloadAndExtractServerDistTask.NAME, DownloadAndExtractServerDistTask)
        project.tasks.create(ExportDatabaseTask.NAME, ExportDatabaseTask)
        project.tasks.create(GenerateSecureAkkaKeysTask.NAME, GenerateSecureAkkaKeysTask)
        project.tasks.create(ImportDbUnitDataTask.NAME, ImportDbUnitDataTask)
        project.tasks.create(PrepareDatabaseTask.NAME, PrepareDatabaseTask)
        project.tasks.create(PrepareDeployTask.NAME, PrepareDeployTask)
        project.tasks.create(RunDatasetGenerationTask.NAME, RunDatasetGenerationTask)
        project.tasks.create(RunDevOpsAsCodeTask.NAME, RunDevOpsAsCodeTask)
        project.tasks.create(SetLogbackLevelsTask.NAME, SetLogbackLevelsTask)
        project.tasks.create(TlsApplicationConfigurationOverrideTask.NAME, TlsApplicationConfigurationOverrideTask)
        project.tasks.create(YamlPatchTask.NAME, YamlPatchTask)

        //Infrastructure
        project.tasks.create(GitlabStartTask.NAME, GitlabStartTask)
        project.tasks.create(GitlabStopTask.NAME, GitlabStopTask)

        //Integration Server
        project.tasks.create(ShutdownIntegrationServerTask.NAME, ShutdownIntegrationServerTask)
        project.tasks.create(StartIntegrationServerTask.NAME, StartIntegrationServerTask).dependsOn(itcfg)

        //MQ
        project.tasks.create(ShutdownMqTask.NAME, ShutdownMqTask)
        project.tasks.create(StartMqTask.NAME, StartMqTask)

        // Plugin Manager
        project.tasks.create(StartPluginManagerTask.NAME, StartPluginManagerTask)

        //Satellite
        project.tasks.create(DownloadAndExtractSatelliteDistTask.NAME, DownloadAndExtractSatelliteDistTask)
        project.tasks.create(PrepareSatellitesTask.NAME, PrepareSatellitesTask)
        project.tasks.create(SatelliteOverlaysTask.NAME, SatelliteOverlaysTask)
        project.tasks.create(ShutdownSatelliteTask.NAME, ShutdownSatelliteTask)
        project.tasks.create(StartSatelliteTask.NAME, StartSatelliteTask)

        //Workers
        project.tasks.create(CopyIntegrationServerTask.NAME, CopyIntegrationServerTask)
        project.tasks.create(SyncServerPluginsWithWorkerTask.NAME, SyncServerPluginsWithWorkerTask)
        project.tasks.create(DownloadAndExtractWorkerDistTask.NAME, DownloadAndExtractWorkerDistTask)
        project.tasks.create(SetWorkersLogbackLevelsTask.NAME, SetWorkersLogbackLevelsTask)
        project.tasks.create(ShutdownWorkersTask.NAME, ShutdownWorkersTask)
        project.tasks.create(StartWorkersTask.NAME, StartWorkersTask)
        project.tasks.create(WorkerOverlaysTask.NAME, WorkerOverlaysTask)

        //Tests
        project.tasks.create(IntegrationTestsTask.NAME, IntegrationTestsTask)
    }

    private static applyDerbyPlugin(Project project) {
        def database = DbUtil.getDatabase(project)

        project.plugins.apply('derby-ns')
        def derbyExtension = project.extensions.getByName("derby")
        derbyExtension.dataDir = DeployServerUtil.getServerWorkingDir(project)
        derbyExtension.port = database.derbyPort
        def startDerbyTask = project.tasks.getByName("derbyStart")
        def stopDerbyTask = project.tasks.getByName("derbyStop")
        TaskUtil.dontFailOnException(stopDerbyTask)
        stopDerbyTask.actions.each { startDerbyTask.doFirst { it } }
        startDerbyTask.mustRunAfter(ApplicationConfigurationOverrideTask.NAME)
    }

    private static void applyPlugins(Project project) {
        applyDerbyPlugin(project)
    }

    @Override
    void apply(Project project) {
        def serverConfig = project.configurations.create(ConfigurationsUtil.DEPLOY_SERVER)
        ConfigurationsUtil.registerConfigurations(project)

        project.configure(project) {
            ExtensionUtil.createExtension(project)
        }

        project.afterEvaluate {
            if (ServerUtil.isServerDefined(project)) {
                ExtensionUtil.initialize(project)
                createTasks(project, serverConfig)
                applyPlugins(project)
            } else {
                project.logger.lifecycle("Nothing to do, a configuration for a server has not found.")
            }
        }
    }
}
