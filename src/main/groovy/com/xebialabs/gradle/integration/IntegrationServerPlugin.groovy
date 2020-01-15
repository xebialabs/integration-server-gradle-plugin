package com.xebialabs.gradle.integration

import com.xebialabs.gradle.integration.tasks.CheckUILibVersionsTask
import com.xebialabs.gradle.integration.tasks.CopyOverlaysTask
import com.xebialabs.gradle.integration.tasks.DownloadAndExtractCliDistTask
import com.xebialabs.gradle.integration.tasks.DownloadAndExtractServerDistTask
import com.xebialabs.gradle.integration.tasks.StartIntegrationServerTask
import com.xebialabs.gradle.integration.tasks.PrepareDatabaseTask
import com.xebialabs.gradle.integration.tasks.SetLogbackLevelsTask
import com.xebialabs.gradle.integration.tasks.ShutdownIntegrationServerTask
import com.xebialabs.gradle.integration.util.ConfigurationsUtil
import com.xebialabs.gradle.integration.util.ExtensionsUtil
import com.xebialabs.gradle.integration.util.TaskUtil
import org.gradle.api.Plugin
import org.gradle.api.Project

class IntegrationServerPlugin implements Plugin<Project> {
    private static void createTasks(Project project) {
        project.tasks.create(DownloadAndExtractServerDistTask.NAME, DownloadAndExtractServerDistTask)
        project.tasks.create(DownloadAndExtractCliDistTask.NAME, DownloadAndExtractCliDistTask)
        project.tasks.create(CopyOverlaysTask.NAME, CopyOverlaysTask)
        project.tasks.create(StartIntegrationServerTask.NAME, StartIntegrationServerTask)
        project.tasks.create(ShutdownIntegrationServerTask.NAME, ShutdownIntegrationServerTask)
        project.tasks.create(PrepareDatabaseTask.NAME, PrepareDatabaseTask)
        project.tasks.create(SetLogbackLevelsTask.NAME, SetLogbackLevelsTask)
        project.tasks.create(CheckUILibVersionsTask.NAME, CheckUILibVersionsTask)
    }

    private static applyDerbyPlugin(Project project) {
        def extension = ExtensionsUtil.getExtension(project)

        project.plugins.apply('derby-ns')
        def derbyExtension = project.extensions.getByName("derby")
        derbyExtension.dataDir = "${ExtensionsUtil.getServerWorkingDir(project)}/derbydb"
        derbyExtension.port = extension.derbyPort
        def startDerbyTask = project.tasks.getByName("derbyStart")
        def stopDerbyTask = project.tasks.getByName("derbyStop")
        startDerbyTask.dependsOn(stopDerbyTask.name)
        TaskUtil.dontFailOnException(stopDerbyTask)
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
