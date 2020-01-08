package com.xebialabs.gradle.integration

import com.xebialabs.gradle.integration.tasks.CopyOverlays
import com.xebialabs.gradle.integration.tasks.DownloadAndExtractCliDistTask
import com.xebialabs.gradle.integration.tasks.DownloadAndExtractServerDistTask
import com.xebialabs.gradle.integration.util.ConfigurationsUtil
import com.xebialabs.gradle.integration.util.ExtensionsUtil
import org.gradle.api.Plugin
import org.gradle.api.Project

class IntegrationServerPlugin implements Plugin<Project> {
    private static void createTasks(Project project) {
        project.tasks.create(DownloadAndExtractServerDistTask.NAME, DownloadAndExtractServerDistTask)
        project.tasks.create(DownloadAndExtractCliDistTask.NAME, DownloadAndExtractCliDistTask)
        project.tasks.create(CopyOverlays.NAME, CopyOverlays)
    }

    private static applyDerbyPlugin(Project project) {
        project.plugins.apply('derby-ns')
        project.extensions.getByName("derby").dataDir = "${ExtensionsUtil.getServerWorkingDir(project)}/derbydb"
    }

    private static void applyPlugins(Project project) {
        applyDerbyPlugin(project)
    }

    @Override
    void apply(Project project) {
        ConfigurationsUtil.registerConfigurations(project)
        ExtensionsUtil.createAndInitialize(project)
        applyPlugins(project)
        createTasks(project)
    }
}
