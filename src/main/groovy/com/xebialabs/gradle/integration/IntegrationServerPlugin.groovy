package com.xebialabs.gradle.integration

import com.xebialabs.gradle.integration.tasks.*
import com.xebialabs.gradle.integration.tasks.anonymizer.ExportDatabaseTask
import com.xebialabs.gradle.integration.tasks.centralconfig.DownloadAndExtractConfigServerDistTask
import com.xebialabs.gradle.integration.tasks.centralconfig.ShutDownConfigServerTask
import com.xebialabs.gradle.integration.tasks.centralconfig.StartConfigServerTask
import com.xebialabs.gradle.integration.tasks.cli.RunProvisionScriptTask
import com.xebialabs.gradle.integration.tasks.database.DockerComposeDatabaseStartTask
import com.xebialabs.gradle.integration.tasks.database.DockerComposeDatabaseStopTask
import com.xebialabs.gradle.integration.tasks.database.ImportDbUnitDataTask
import com.xebialabs.gradle.integration.tasks.database.PrepareDatabaseTask
import com.xebialabs.gradle.integration.tasks.gitlab.DockerComposeGitlabStartTask
import com.xebialabs.gradle.integration.tasks.gitlab.DockerComposeGitlabStopTask
import com.xebialabs.gradle.integration.tasks.mq.ShutdownMq
import com.xebialabs.gradle.integration.tasks.mq.StartMq
import com.xebialabs.gradle.integration.tasks.pluginManager.StartPluginManagerTask
import com.xebialabs.gradle.integration.tasks.satellite.CopySatelliteOverlaysTask
import com.xebialabs.gradle.integration.tasks.satellite.DownloadAndExtractSatelliteDistTask
import com.xebialabs.gradle.integration.tasks.satellite.ShutdownSatelliteTask
import com.xebialabs.gradle.integration.tasks.satellite.StartSatelliteTask
import com.xebialabs.gradle.integration.tasks.worker.ShutdownWorker
import com.xebialabs.gradle.integration.tasks.worker.StartWorker
import com.xebialabs.gradle.integration.util.ConfigurationsUtil
import com.xebialabs.gradle.integration.util.ExtensionsUtil
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
        project.tasks.create(DockerComposeDatabaseStartTask.NAME, DockerComposeDatabaseStartTask)
        project.tasks.create(DockerComposeDatabaseStopTask.NAME, DockerComposeDatabaseStopTask)
        project.tasks.create(DockerComposeGitlabStartTask.NAME, DockerComposeGitlabStartTask)
        project.tasks.create(DockerComposeGitlabStopTask.NAME, DockerComposeGitlabStopTask)

        project.tasks.create(DownloadAndExtractCliDistTask.NAME, DownloadAndExtractCliDistTask)
        project.tasks.create(DownloadAndExtractConfigServerDistTask.NAME, DownloadAndExtractConfigServerDistTask)
        project.tasks.create(DownloadAndExtractDbUnitDataDistTask.NAME, DownloadAndExtractDbUnitDataDistTask)
        project.tasks.create(DownloadAndExtractServerDistTask.NAME, DownloadAndExtractServerDistTask)
        project.tasks.create(DownloadAndExtractSatelliteDistTask.NAME, DownloadAndExtractSatelliteDistTask)
        project.tasks.create(ExportDatabaseTask.NAME, ExportDatabaseTask)
        project.tasks.create(ImportDbUnitDataTask.NAME, ImportDbUnitDataTask)
        project.tasks.create(IntegrationServerTestTask.NAME, IntegrationServerTestTask)
        project.tasks.create(PrepareDatabaseTask.NAME, PrepareDatabaseTask)
        project.tasks.create(RemoveStdoutConfigTask.NAME, RemoveStdoutConfigTask)
        project.tasks.create(RunProvisionScriptTask.NAME, RunProvisionScriptTask).dependsOn(clicfg)
        project.tasks.create(StartMq.NAME, StartMq)
        project.tasks.create(ShutdownMq.NAME, ShutdownMq)
        project.tasks.create(StartWorker.NAME, StartWorker)
        project.tasks.create(ShutdownWorker.NAME, ShutdownWorker)

        project.tasks.create(SetLogbackLevelsTask.NAME, SetLogbackLevelsTask)
        project.tasks.create(ShutDownConfigServerTask.NAME, ShutDownConfigServerTask)
        project.tasks.create(StartConfigServerTask.NAME, StartConfigServerTask)
        project.tasks.create(ShutdownIntegrationServerTask.NAME, ShutdownIntegrationServerTask)
        project.tasks.create(StartIntegrationServerTask.NAME, StartIntegrationServerTask).dependsOn(itcfg)
        project.tasks.create(ShutdownSatelliteTask.NAME, ShutdownSatelliteTask)
        project.tasks.create(StartSatelliteTask.NAME, StartSatelliteTask)
        project.tasks.create(StartPluginManagerTask.NAME, StartPluginManagerTask)
        project.tasks.create(YamlPatchTask.NAME, YamlPatchTask)
    }

    private static applyDerbyPlugin(Project project) {
        def extension = ExtensionsUtil.getExtension(project)

        project.plugins.apply('derby-ns')
        def derbyExtension = project.extensions.getByName("derby")
        derbyExtension.dataDir = "${ExtensionsUtil.getServerWorkingDir(project)}/derbydb"
        derbyExtension.port = extension.derbyPort
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
        def serverConfig = project.configurations.create(ConfigurationsUtil.INTEGRATION_TEST_SERVER)
        def cliConfig = project.configurations.create(ConfigurationsUtil.INTEGRATION_TEST_CLI)
        ConfigurationsUtil.registerConfigurations(project)
        ExtensionsUtil.create(project)

        project.afterEvaluate {
            ExtensionsUtil.initialize(project)
            createTasks(project, serverConfig, cliConfig)
            applyPlugins(project)
        }
    }
}
