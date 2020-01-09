package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.util.ExtensionsUtil
import org.gradle.api.tasks.Copy

import static com.xebialabs.gradle.integration.util.PluginUtil.*
import static com.xebialabs.gradle.integration.util.ConfigurationsUtil.SERVER_CLI_DIST_CONFIG

class DownloadAndExtractCliDistTask extends Copy {
    static NAME = "downloadAndExtractCli"

    DownloadAndExtractCliDistTask() {
        this.configure {
            group = PLUGIN_GROUP
            def serverVersion = ExtensionsUtil.getExtension(project).serverVersion
            project.buildscript.dependencies.add(
                    SERVER_CLI_DIST_CONFIG,
                    "com.xebialabs.deployit:xl-deploy-base:${serverVersion}:cli@zip"
            )
            from { project.zipTree(project.buildscript.configurations.getByName(SERVER_CLI_DIST_CONFIG).singleFile) }
            into { project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString() }
        }
    }
}
