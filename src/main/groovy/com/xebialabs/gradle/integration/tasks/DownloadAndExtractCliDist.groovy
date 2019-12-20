package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.IntegrationServerExtension
import org.gradle.api.tasks.Copy

import static com.xebialabs.gradle.integration.util.PluginUtils.*
import static com.xebialabs.gradle.integration.util.ConfigurationsUtil.SERVER_CLI_DIST_CONFIG

class DownloadAndExtractCliDist extends Copy {
    DownloadAndExtractCliDist() {
        this.configure {
            group = PLUGIN_GROUP
            def serverVersion = project.extensions.getByType(IntegrationServerExtension).serverVersion
            project.buildscript.dependencies.add(
                    SERVER_CLI_DIST_CONFIG,
                    "com.xebialabs.deployit:xl-deploy-base:${serverVersion}:cli@zip"
            )
            from { project.zipTree(project.buildscript.configurations.getByName(SERVER_CLI_DIST_CONFIG).singleFile) }
            into { project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString() }
        }
    }
}
