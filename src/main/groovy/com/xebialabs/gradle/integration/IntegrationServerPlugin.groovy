package com.xebialabs.gradle.integration

import com.xebialabs.gradle.integration.tasks.*
import com.xebialabs.gradle.integration.tasks.database.DockerComposeDatabaseStartTask
import com.xebialabs.gradle.integration.tasks.database.DockerComposeDatabaseStopTask
import com.xebialabs.gradle.integration.tasks.database.ImportDbUnitDataTask
import com.xebialabs.gradle.integration.tasks.database.PrepareDatabaseTask
import com.xebialabs.gradle.integration.tasks.satellite.StartSatelliteTask
import com.xebialabs.gradle.integration.tasks.satellite.StopSatelliteTask
import com.xebialabs.gradle.integration.util.ConfigurationsUtil
import com.xebialabs.gradle.integration.util.ExtensionsUtil
import com.xebialabs.gradle.integration.util.TaskUtil
import org.gradle.api.Plugin
import org.gradle.api.Project

class IntegrationServerPlugin implements Plugin<Project> {
    private static void createTasks(Project project) {
        project.tasks.create(DownloadAndExtractServerDistTask.NAME, DownloadAndExtractServerDistTask)
        project.tasks.create(DownloadAndExtractCliDistTask.NAME, DownloadAndExtractCliDistTask)
        project.tasks.create(DownloadAndExtractSatelliteDistTask.NAME, DownloadAndExtractSatelliteDistTask)
        project.tasks.create(CopyOverlaysTask.NAME, CopyOverlaysTask)
        project.tasks.create(StartIntegrationServerTask.NAME, StartIntegrationServerTask)
        project.tasks.create(StartSatelliteTask.NAME, StartSatelliteTask)
        project.tasks.create(ShutdownIntegrationServerTask.NAME, ShutdownIntegrationServerTask)
        project.tasks.create(StopSatelliteTask.NAME, StopSatelliteTask)
        project.tasks.create(PrepareDatabaseTask.NAME, PrepareDatabaseTask)
        project.tasks.create(SetLogbackLevelsTask.NAME, SetLogbackLevelsTask)
        project.tasks.create(CheckUILibVersionsTask.NAME, CheckUILibVersionsTask)
        project.tasks.create(ImportDbUnitDataTask.NAME, ImportDbUnitDataTask)
        project.tasks.create(DownloadAndExtractDbUnitDataDistTask.NAME, DownloadAndExtractDbUnitDataDistTask)
        project.tasks.create(DockerComposeDatabaseStartTask.NAME, DockerComposeDatabaseStartTask)
        project.tasks.create(DockerComposeDatabaseStopTask.NAME, DockerComposeDatabaseStopTask)
        project.tasks.create(StartRabbitMq.NAME, StartRabbitMq)
        project.tasks.create(StopRabbitMq.NAME, StopRabbitMq)
        project.tasks.create(StartWorker.NAME, StartWorker)
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
        ConfigurationsUtil.registerConfigurations(project)
        ExtensionsUtil.create(project)

        project.afterEvaluate {
            ExtensionsUtil.initialize(project)
            createTasks(project)
            applyPlugins(project)
        }
    }
}
