package com.xebialabs.gradle.integration

import com.xebialabs.gradle.integration.tasks.DownloadAndExtractServerDist
import com.xebialabs.gradle.integration.util.ConfigurationsUtil
import com.xebialabs.gradle.integration.util.ExtensionsUtil
import org.gradle.api.Plugin
import org.gradle.api.Project

class IntegrationServerPlugin implements Plugin<Project> {
  private void createTasks(Project project) {
    def serverDist = project.tasks.create("downloadAndExtractServerDist", DownloadAndExtractServerDist)
  }

  @Override
  void apply(Project project) {
    ConfigurationsUtil.registerConfigurations(project)
    def extension = ExtensionsUtil.createAndInitialize(project)
    createTasks(project)
  }
}
