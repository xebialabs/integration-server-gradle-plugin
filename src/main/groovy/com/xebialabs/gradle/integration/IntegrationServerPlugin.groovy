package com.xebialabs.gradle.integration

import com.xebialabs.gradle.integration.tasks.*
import com.xebialabs.gradle.integration.tasks.anonymizer.ExportDatabaseTask
import com.xebialabs.gradle.integration.tasks.cli.RunProvisionScriptTask
import com.xebialabs.gradle.integration.tasks.database.DatabaseStartTask
import com.xebialabs.gradle.integration.tasks.database.DatabaseStopTask
import com.xebialabs.gradle.integration.tasks.database.ImportDbUnitDataTask
import com.xebialabs.gradle.integration.tasks.database.PrepareDatabaseTask
import com.xebialabs.gradle.integration.tasks.gitlab.GitlabStartTask
import com.xebialabs.gradle.integration.tasks.gitlab.GitlabStopTask
import com.xebialabs.gradle.integration.tasks.mq.ShutdownMq
import com.xebialabs.gradle.integration.tasks.mq.StartMq
import com.xebialabs.gradle.integration.tasks.pluginManager.StartPluginManagerTask
import com.xebialabs.gradle.integration.tasks.satellite.CopySatelliteOverlaysTask
import com.xebialabs.gradle.integration.tasks.satellite.DownloadAndExtractSatelliteDistTask
import com.xebialabs.gradle.integration.tasks.satellite.ShutdownSatelliteTask
import com.xebialabs.gradle.integration.tasks.satellite.StartSatelliteTask
import com.xebialabs.gradle.integration.tasks.worker.ShutdownWorkers
import com.xebialabs.gradle.integration.tasks.worker.StartWorkers
import com.xebialabs.gradle.integration.util.ConfigurationsUtil
import com.xebialabs.gradle.integration.util.DbUtil
import com.xebialabs.gradle.integration.util.ExtensionUtil
import com.xebialabs.gradle.integration.util.LocationUtil
import com.xebialabs.gradle.integration.util.TaskUtil
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

class IntegrationServerPlugin implements Plugin<Project> {

    private static void createTasks(Project project, Configuration itcfg, Configuration clicfg) {
        project.tasks.create(CentralConfigurationTask.NAME, CentralConfigurationTask)
        project.tasks.create(CheckUILibVersionsTask.NAME, CheckUILibVersionsTask)
        project.tasks.create(CopyOverlaysTask.NAME, CopyOverlaysTask)
        project.tasks.create(CopySatelliteOverlaysTask.NAME, CopySatelliteOverlaysTask)

        project.tasks.create(DeletePrepackagedXldStitchCoreTask.NAME, DeletePrepackagedXldStitchCoreTask)
        project.tasks.create(DatabaseStartTask.NAME, DatabaseStartTask)
        project.tasks.create(DatabaseStopTask.NAME, DatabaseStopTask)
        project.tasks.create(GitlabStartTask.NAME, GitlabStartTask)
        project.tasks.create(GitlabStopTask.NAME, GitlabStopTask)

        project.tasks.create(ExportDatabaseTask.NAME, ExportDatabaseTask)
        project.tasks.create(RunProvisionScriptTask.NAME, RunProvisionScriptTask).dependsOn(clicfg)

        project.tasks.create(ImportDbUnitDataTask.NAME, ImportDbUnitDataTask)
        project.tasks.create(PrepareDatabaseTask.NAME, PrepareDatabaseTask)
        project.tasks.create(RemoveStdoutConfigTask.NAME, RemoveStdoutConfigTask)

        project.tasks.create(ShutdownWorkers.NAME, ShutdownWorkers)
        project.tasks.create(ShutdownIntegrationServerTask.NAME, ShutdownIntegrationServerTask)

        project.tasks.create(DownloadAndExtractCliDistTask.NAME, DownloadAndExtractCliDistTask)
        project.tasks.create(DownloadAndExtractDbUnitDataDistTask.NAME, DownloadAndExtractDbUnitDataDistTask)
        project.tasks.create(DownloadAndExtractServerDistTask.NAME, DownloadAndExtractServerDistTask)
        project.tasks.create(DownloadAndExtractSatelliteDistTask.NAME, DownloadAndExtractSatelliteDistTask)

        project.tasks.create(StartMq.NAME, StartMq)
        project.tasks.create(ShutdownMq.NAME, ShutdownMq)
        project.tasks.create(StartWorkers.NAME, StartWorkers)

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
        derbyExtension.dataDir = "${LocationUtil.getServerWorkingDir(project)}/derbydb"
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
            ExtensionUtil.initialize(project)
            createTasks(project, serverConfig, cliConfig)
            applyPlugins(project)
        }
    }
}
