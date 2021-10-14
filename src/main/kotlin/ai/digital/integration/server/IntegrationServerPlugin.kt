package ai.digital.integration.server

import ai.digital.integration.server.tasks.*
import ai.digital.integration.server.tasks.anonymizer.ExportDatabaseTask
import ai.digital.integration.server.tasks.cli.*
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
import ai.digital.integration.server.tasks.satellite.*
import ai.digital.integration.server.tasks.worker.*
import ai.digital.integration.server.util.ConfigurationsUtil.Companion.DEPLOY_SERVER
import ai.digital.integration.server.util.ConfigurationsUtil.Companion.registerConfigurations
import ai.digital.integration.server.util.DbUtil.Companion.getDatabase
import ai.digital.integration.server.util.DeployServerUtil.Companion.getServerWorkingDir
import ai.digital.integration.server.util.DeployServerUtil.Companion.isServerDefined
import ai.digital.integration.server.util.ExtensionUtil.Companion.createExtension
import ai.digital.integration.server.util.ExtensionUtil.Companion.initialize
import ai.digital.integration.server.util.TaskUtil.Companion.dontFailOnException
import com.xebialabs.gradle.plugins.derby.DerbyExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.kotlin.dsl.closureOf

class IntegrationServerPlugin : Plugin<Project> {

    private fun createTasks(project: Project, itcfg: Configuration) {

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
        project.tasks.create(ApplicationConfigurationOverrideTask.NAME, ApplicationConfigurationOverrideTask::class.java)
        project.tasks.create(CentralConfigurationTask.NAME, CentralConfigurationTask::class.java)
        project.tasks.create(CheckUILibVersionsTask.NAME, CheckUILibVersionsTask::class.java)
        project.tasks.create(CopyServerBuildArtifactsTask.NAME, CopyServerBuildArtifactsTask::class.java)
        project.tasks.create(CopyOverlaysTask.NAME, CopyOverlaysTask::class.java)
        project.tasks.create(DockerBasedStopDeployTask.NAME, DockerBasedStopDeployTask::class.java)
        project.tasks.create(DownloadAndExtractDbUnitDataDistTask.NAME, DownloadAndExtractDbUnitDataDistTask::class.java)
        project.tasks.create(DownloadAndExtractServerDistTask.NAME, DownloadAndExtractServerDistTask::class.java)
        project.tasks.create(ExportDatabaseTask.NAME, ExportDatabaseTask::class.java)
        project.tasks.create(GenerateSecureAkkaKeysTask.NAME, GenerateSecureAkkaKeysTask::class.java)
        project.tasks.create(ImportDbUnitDataTask.NAME, ImportDbUnitDataTask::class.java)
        project.tasks.create(PrepareDatabaseTask.NAME, PrepareDatabaseTask::class.java)
        project.tasks.create(PrepareDeployTask.NAME, PrepareDeployTask::class.java)
        project.tasks.create(RunDatasetGenerationTask.NAME, RunDatasetGenerationTask::class.java)
        project.tasks.create(RunDevOpsAsCodeTask.NAME, RunDevOpsAsCodeTask::class.java)
        project.tasks.create(SetLogbackLevelsTask.NAME, SetLogbackLevelsTask::class.java)
        project.tasks.create(TlsApplicationConfigurationOverrideTask.NAME, TlsApplicationConfigurationOverrideTask::class.java)
        project.tasks.create(YamlPatchTask.NAME, YamlPatchTask::class.java)

        //Infrastructure
        project.tasks.create(GitlabStartTask.NAME, GitlabStartTask::class.java)
        project.tasks.create(GitlabStopTask.NAME, GitlabStopTask::class.java)

        //Integration Server
        project.tasks.create(ShutdownIntegrationServerTask.NAME, ShutdownIntegrationServerTask::class.java)
        project.tasks.create(StartIntegrationServerTask.NAME, StartIntegrationServerTask::class.java).dependsOn(itcfg)

        //MQ
        project.tasks.create(ShutdownMqTask.NAME, ShutdownMqTask::class.java)
        project.tasks.create(StartMqTask.NAME, StartMqTask::class.java)

        // Plugin Manager
        project.tasks.create(StartPluginManagerTask.NAME, StartPluginManagerTask::class.java)

        //Satellite
        project.tasks.create(DownloadAndExtractSatelliteDistTask.NAME, DownloadAndExtractSatelliteDistTask::class.java)
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

    private fun applyDerbyPlugin(project: Project): Task {
        val database = getDatabase(project)
        project.plugins.apply("derby-ns")
        val derbyExtension = project.extensions.getByName("derby") as DerbyExtension
        derbyExtension.dataDir = getServerWorkingDir(project)
        derbyExtension.port = database.derbyPort!!
        val startDerbyTask = project.tasks.getByName("derbyStart")
        val stopDerbyTask = project.tasks.getByName("derbyStop")
        dontFailOnException(stopDerbyTask)
        stopDerbyTask.actions.forEach { action ->
            return startDerbyTask.doFirst(action)
        }
        return startDerbyTask.mustRunAfter(ApplicationConfigurationOverrideTask.NAME)
    }

    private fun applyPlugins(project: Project) {
        applyDerbyPlugin(project)
    }

    override fun apply(project: Project) {
        val serverConfig = project.configurations.create(DEPLOY_SERVER)
        registerConfigurations(project)
        project.configure(project, closureOf<Project> {
            createExtension(project)
        })
        project.afterEvaluate {
            if (isServerDefined(project)) {
                initialize(project)
                createTasks(project, serverConfig)
                applyPlugins(project)
            } else {
                project.logger.lifecycle("Nothing to do, a configuration for a server has not found.")
            }
        }
    }
}
