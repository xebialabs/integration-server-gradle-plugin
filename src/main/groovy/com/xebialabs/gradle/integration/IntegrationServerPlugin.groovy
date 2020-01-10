package com.xebialabs.gradle.integration

import com.xebialabs.gradle.integration.tasks.CopyOverlays
import com.xebialabs.gradle.integration.tasks.DownloadAndExtractCliDistTask
import com.xebialabs.gradle.integration.tasks.DownloadAndExtractServerDistTask
import com.xebialabs.gradle.integration.tasks.LaunchIntegrationServerTask
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
        project.tasks.create(CopyOverlays.NAME, CopyOverlays)
        project.tasks.create(LaunchIntegrationServerTask.NAME, LaunchIntegrationServerTask)
        project.tasks.create(ShutdownIntegrationServerTask.NAME, ShutdownIntegrationServerTask)
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
        startDerbyTask.mustRunAfter(CopyOverlays.NAME)
    }

    private static void applyPlugins(Project project) {
        applyDerbyPlugin(project)
    }

    @Override
    void apply(Project project) {
        ConfigurationsUtil.registerConfigurations(project)
        ExtensionsUtil.createAndInitialize(project)

        project.afterEvaluate {
            createTasks(project)
            applyPlugins(project)
        }
    }
}
