package com.xebialabs.gradle.integration

import com.xebialabs.gradle.integration.tasks.CopyLicenseTask
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
    project.tasks.create(CopyLicenseTask.NAME, CopyLicenseTask)
  }

  @Override
  void apply(Project project) {
    ConfigurationsUtil.registerConfigurations(project)
    ExtensionsUtil.createAndInitialize(project)
    createTasks(project)
  }
}
