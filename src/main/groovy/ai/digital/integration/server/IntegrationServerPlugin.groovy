package ai.digital.integration.server

import ai.digital.integration.server.tasks.*
import ai.digital.integration.server.tasks.anonymizer.ExportDatabaseTask
import ai.digital.integration.server.tasks.cli.RunProvisionScriptTask
import ai.digital.integration.server.tasks.database.DatabaseStartTask
import ai.digital.integration.server.tasks.database.DatabaseStopTask
import ai.digital.integration.server.tasks.database.ImportDbUnitDataTask
import ai.digital.integration.server.tasks.database.PrepareDatabaseTask
import ai.digital.integration.server.tasks.gitlab.GitlabStartTask
import ai.digital.integration.server.tasks.gitlab.GitlabStopTask
import ai.digital.integration.server.tasks.mq.ShutdownMqTask
import ai.digital.integration.server.tasks.mq.StartMqTask
import ai.digital.integration.server.tasks.pluginManager.StartPluginManagerTask
import ai.digital.integration.server.tasks.satellite.CopySatelliteOverlaysTask
import ai.digital.integration.server.tasks.satellite.DownloadAndExtractSatelliteDistTask
import ai.digital.integration.server.tasks.satellite.ShutdownSatelliteTask
import ai.digital.integration.server.tasks.satellite.StartSatelliteTask
import ai.digital.integration.server.tasks.worker.ShutdownWorkersTask
import ai.digital.integration.server.tasks.worker.StartWorkersTask
import ai.digital.integration.server.util.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

class IntegrationServerPlugin implements Plugin<Project> {

    private static void createTasks(Project project, Configuration itcfg, Configuration clicfg) {
        project.tasks.create(CentralConfigurationTask.NAME, CentralConfigurationTask)
        project.tasks.create(CheckUILibVersionsTask.NAME, CheckUILibVersionsTask)
        project.tasks.create(CopyOverlaysTask.NAME, CopyOverlaysTask)
        project.tasks.create(CopySatelliteOverlaysTask.NAME, CopySatelliteOverlaysTask)

        project.tasks.create(DockerBasedStopDeployTask.NAME, DockerBasedStopDeployTask)

        project.tasks.create(DatabaseStartTask.NAME, DatabaseStartTask)
        project.tasks.create(DatabaseStopTask.NAME, DatabaseStopTask)
        project.tasks.create(GitlabStartTask.NAME, GitlabStartTask)
        project.tasks.create(GitlabStopTask.NAME, GitlabStopTask)

        project.tasks.create(ExportDatabaseTask.NAME, ExportDatabaseTask)
        project.tasks.create(RunProvisionScriptTask.NAME, RunProvisionScriptTask).dependsOn(clicfg)

        project.tasks.create(ImportDbUnitDataTask.NAME, ImportDbUnitDataTask)
        project.tasks.create(PrepareDatabaseTask.NAME, PrepareDatabaseTask)
        project.tasks.create(PrepareDeployTask.NAME, PrepareDeployTask)
        project.tasks.create(RemoveStdoutConfigTask.NAME, RemoveStdoutConfigTask)

        project.tasks.create(ShutdownWorkersTask.NAME, ShutdownWorkersTask)
        project.tasks.create(ShutdownIntegrationServerTask.NAME, ShutdownIntegrationServerTask)

        project.tasks.create(DownloadAndExtractCliDistTask.NAME, DownloadAndExtractCliDistTask)
        project.tasks.create(DownloadAndExtractDbUnitDataDistTask.NAME, DownloadAndExtractDbUnitDataDistTask)
        project.tasks.create(DownloadAndExtractServerDistTask.NAME, DownloadAndExtractServerDistTask)
        project.tasks.create(DownloadAndExtractSatelliteDistTask.NAME, DownloadAndExtractSatelliteDistTask)

        project.tasks.create(StartMqTask.NAME, StartMqTask)
        project.tasks.create(ShutdownMqTask.NAME, ShutdownMqTask)
        project.tasks.create(StartWorkersTask.NAME, StartWorkersTask)

        project.tasks.create(SetLogbackLevelsTask.NAME, SetLogbackLevelsTask)
        project.tasks.create(StartIntegrationServerTask.NAME, StartIntegrationServerTask).dependsOn(itcfg)
        project.tasks.create(ShutdownSatelliteTask.NAME, ShutdownSatelliteTask)
        project.tasks.create(StartSatelliteTask.NAME, StartSatelliteTask)
        project.tasks.create(StartPluginManagerTask.NAME, StartPluginManagerTask)

        project.tasks.create(YamlPatchTask.NAME, YamlPatchTask)
    }

    private static applyDerbyPlugin(Project project) {
        def database = DbUtil.getDatabase(project)

        project.plugins.apply('derby-ns')
        def derbyExtension = project.extensions.getByName("derby")
        derbyExtension.dataDir = "${ServerUtil.getServerWorkingDir(project)}/derbydb"
        derbyExtension.port = database.derbyPort
        def startDerbyTask = project.tasks.getByName("derbyStart")
        def stopDerbyTask = project.tasks.getByName("derbyStop")
        TaskUtil.dontFailOnException(stopDerbyTask)
        stopDerbyTask.actions.each { startDerbyTask.doFirst { it } }
        startDerbyTask.mustRunAfter(CopyOverlaysTask.NAME)
    }

    private static void applyPlugins(Project project) {
        applyDerbyPlugin(project)
    }

    @Override
    void apply(Project project) {
        def serverConfig = project.configurations.create(ConfigurationsUtil.DEPLOY_SERVER)
        def cliConfig = project.configurations.create(ConfigurationsUtil.DEPLOY_CLI)
        ConfigurationsUtil.registerConfigurations(project)

        project.configure(project) {
            ExtensionUtil.createExtension(project)
        }

        project.afterEvaluate {
            if (ServerUtil.isServerDefined(project)) {
                ExtensionUtil.initialize(project)
                createTasks(project, serverConfig, cliConfig)
                applyPlugins(project)
            } else {
                project.logger.lifecycle("Nothing to do, a configuration for a server has not found.")
            }
        }
    }
}
