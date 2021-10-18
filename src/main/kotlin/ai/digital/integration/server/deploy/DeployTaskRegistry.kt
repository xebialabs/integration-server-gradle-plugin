package ai.digital.integration.server.deploy

import ai.digital.integration.server.deploy.tasks.*
import ai.digital.integration.server.deploy.tasks.anonymizer.ExportDatabaseTask
import ai.digital.integration.server.deploy.tasks.cli.*
import ai.digital.integration.server.common.tasks.database.DatabaseStartTask
import ai.digital.integration.server.common.tasks.database.DatabaseStopTask
import ai.digital.integration.server.common.tasks.database.ImportDbUnitDataTask
import ai.digital.integration.server.common.tasks.database.PrepareDatabaseTask
import ai.digital.integration.server.common.gitlab.GitlabStartTask
import ai.digital.integration.server.common.gitlab.GitlabStopTask
import ai.digital.integration.server.common.mq.ShutdownMqTask
import ai.digital.integration.server.common.mq.StartMqTask
import ai.digital.integration.server.common.pluginManager.StartPluginManagerTask
import ai.digital.integration.server.deploy.tasks.provision.RunDatasetGenerationTask
import ai.digital.integration.server.deploy.tasks.provision.RunDevOpsAsCodeTask
import ai.digital.integration.server.deploy.tasks.satellite.*
import ai.digital.integration.server.deploy.tasks.worker.*
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

open class DeployTaskRegistry {

    companion object {
        fun register(project: Project, itcfg: Configuration) {

            //CLI
            project.tasks.create(CliCleanDefaultExtTask.NAME, CliCleanDefaultExtTask::class.java)
            project.tasks.create(CopyCliBuildArtifactsTask.NAME, CopyCliBuildArtifactsTask::class.java)
            project.tasks.create(CliOverlaysTask.NAME, CliOverlaysTask::class.java)
            project.tasks.create(DownloadAndExtractCliDistTask.NAME, DownloadAndExtractCliDistTask::class.java)
            project.tasks.create(RunCliTask.NAME, RunCliTask::class.java)

            //Database
            project.tasks.create(DatabaseStartTask.NAME, DatabaseStartTask::class.java)
            project.tasks.create(DatabaseStopTask.NAME, DatabaseStopTask::class.java)

            //Deploy Server
            project.tasks.create(ApplicationConfigurationOverrideTask.NAME,
                ApplicationConfigurationOverrideTask::class.java)
            project.tasks.create(CentralConfigurationTask.NAME, CentralConfigurationTask::class.java)
            project.tasks.create(CheckUILibVersionsTask.NAME, CheckUILibVersionsTask::class.java)
            project.tasks.create(CopyServerBuildArtifactsTask.NAME, CopyServerBuildArtifactsTask::class.java)
            project.tasks.create(CopyOverlaysTask.NAME, CopyOverlaysTask::class.java)
            project.tasks.create(DockerBasedStopDeployTask.NAME, DockerBasedStopDeployTask::class.java)
            project.tasks.create(DownloadAndExtractDbUnitDataDistTask.NAME,
                DownloadAndExtractDbUnitDataDistTask::class.java)
            project.tasks.create(DownloadAndExtractServerDistTask.NAME, DownloadAndExtractServerDistTask::class.java)
            project.tasks.create(ExportDatabaseTask.NAME, ExportDatabaseTask::class.java)
            project.tasks.create(GenerateSecureAkkaKeysTask.NAME, GenerateSecureAkkaKeysTask::class.java)
            project.tasks.create(ImportDbUnitDataTask.NAME, ImportDbUnitDataTask::class.java)
            project.tasks.create(PrepareDatabaseTask.NAME, PrepareDatabaseTask::class.java)
            project.tasks.create(PrepareDeployTask.NAME, PrepareDeployTask::class.java)
            project.tasks.create(RunDatasetGenerationTask.NAME, RunDatasetGenerationTask::class.java)
            project.tasks.create(RunDevOpsAsCodeTask.NAME, RunDevOpsAsCodeTask::class.java)
            project.tasks.create(SetLogbackLevelsTask.NAME, SetLogbackLevelsTask::class.java)
            project.tasks.create(TlsApplicationConfigurationOverrideTask.NAME,
                TlsApplicationConfigurationOverrideTask::class.java)
            project.tasks.create(YamlPatchTask.NAME, YamlPatchTask::class.java)

            //Infrastructure
            project.tasks.create(GitlabStartTask.NAME, GitlabStartTask::class.java)
            project.tasks.create(GitlabStopTask.NAME, GitlabStopTask::class.java)

            //Integration Server
            project.tasks.create(ShutdownDeployIntegrationServerTask.NAME, ShutdownDeployIntegrationServerTask::class.java)
            project.tasks.create(StartDeployIntegrationServerTask.NAME, StartDeployIntegrationServerTask::class.java)
                .dependsOn(itcfg)

            //MQ
            project.tasks.create(ShutdownMqTask.NAME, ShutdownMqTask::class.java)
            project.tasks.create(StartMqTask.NAME, StartMqTask::class.java)

            // Plugin Manager
            project.tasks.create(StartPluginManagerTask.NAME, StartPluginManagerTask::class.java)

            //Satellite
            project.tasks.create(DownloadAndExtractSatelliteDistTask.NAME,
                DownloadAndExtractSatelliteDistTask::class.java)
            project.tasks.create(PrepareSatellitesTask.NAME, PrepareSatellitesTask::class.java)
            project.tasks.create(SatelliteOverlaysTask.NAME, SatelliteOverlaysTask::class.java)
            project.tasks.create(SatelliteSyncPluginsTask.NAME, SatelliteSyncPluginsTask::class.java)
            project.tasks.create(ShutdownSatelliteTask.NAME, ShutdownSatelliteTask::class.java)
            project.tasks.create(StartSatelliteTask.NAME, StartSatelliteTask::class.java)

            //Workers
            project.tasks.create(CopyIntegrationServerTask.NAME, CopyIntegrationServerTask::class.java)
            project.tasks.create(SyncServerPluginsWithWorkerTask.NAME, SyncServerPluginsWithWorkerTask::class.java)
            project.tasks.create(DownloadAndExtractWorkerDistTask.NAME, DownloadAndExtractWorkerDistTask::class.java)
            project.tasks.create(SetWorkersLogbackLevelsTask.NAME, SetWorkersLogbackLevelsTask::class.java)
            project.tasks.create(ShutdownWorkersTask.NAME, ShutdownWorkersTask::class.java)
            project.tasks.create(StartWorkersTask.NAME, StartWorkersTask::class.java)
            project.tasks.create(WorkerOverlaysTask.NAME, WorkerOverlaysTask::class.java)

            //Tests
            project.tasks.create(IntegrationTestsTask.NAME, IntegrationTestsTask::class.java)
        }
    }
}
