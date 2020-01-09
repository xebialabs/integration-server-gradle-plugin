package com.xebialabs.gradle.integration

import com.xebialabs.gradle.integration.tasks.CopyOverlays
import com.xebialabs.gradle.integration.tasks.DownloadAndExtractCliDistTask
import com.xebialabs.gradle.integration.tasks.DownloadAndExtractServerDistTask
import com.xebialabs.gradle.integration.tasks.LaunchIntegrationServerTask
import com.xebialabs.gradle.integration.util.ConfigurationsUtil
import com.xebialabs.gradle.integration.util.ExtensionsUtil
import org.gradle.api.Plugin
import org.gradle.api.Project

class IntegrationServerPlugin implements Plugin<Project> {
    private static void createTasks(Project project) {
        project.tasks.create(DownloadAndExtractServerDistTask.NAME, DownloadAndExtractServerDistTask)
        project.tasks.create(DownloadAndExtractCliDistTask.NAME, DownloadAndExtractCliDistTask)
        project.tasks.create(CopyOverlays.NAME, CopyOverlays)
        project.tasks.create(LaunchIntegrationServerTask.NAME, LaunchIntegrationServerTask)
    }

    private static applyDerbyPlugin(Project project, IntegrationServerExtension extension) {
        project.plugins.apply('derby-ns')
        def derbyExtension = project.extensions.getByName("derby")
        derbyExtension.dataDir = "${ExtensionsUtil.getServerWorkingDir(project)}/derbydb"
        derbyExtension.port = extension.derbyPort
        def derbyTask = project.tasks.getByName("derbyStart")
        derbyTask.mustRunAfter(CopyOverlays.NAME)
    }

    private static void applyPlugins(Project project, IntegrationServerExtension extension) {
        applyDerbyPlugin(project, extension)
    }

    @Override
    void apply(Project project) {
        ConfigurationsUtil.registerConfigurations(project)
        def extension = ExtensionsUtil.createAndInitialize(project)
        applyPlugins(project, extension)
        createTasks(project)
    }
}
