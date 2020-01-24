package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.util.ExtensionsUtil
import org.gradle.api.tasks.Copy

import static com.xebialabs.gradle.integration.util.ConfigurationsUtil.SERVER_DATA_DIST
import static com.xebialabs.gradle.integration.util.PluginUtil.DIST_DESTINATION_NAME
import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class DownloadAndExtractDataDistTask extends Copy {
    static NAME = "downloadAndExtractData"

    DownloadAndExtractDataDistTask() {
        this.configure {
            group = PLUGIN_GROUP
            def serverVersion = '9.6.1-SNAPSHOT' //ExtensionsUtil.getExtension(project).serverVersion
            project.buildscript.dependencies.add(
                SERVER_DATA_DIST,
                "com.xebialabs.deployit.plugins:xld-is-data:${serverVersion}:repository@zip"
            )
            from { project.zipTree(project.buildscript.configurations.getByName(SERVER_DATA_DIST).singleFile) }
            into { project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString() }
        }
    }
}
