package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.util.ExtensionUtil
import org.gradle.api.tasks.Copy

import static com.xebialabs.gradle.integration.util.ConfigurationsUtil.SERVER_DATA_DIST
import static com.xebialabs.gradle.integration.constant.PluginConstant.DIST_DESTINATION_NAME
import static com.xebialabs.gradle.integration.constant.PluginConstant.PLUGIN_GROUP

class DownloadAndExtractDbUnitDataDistTask extends Copy {
    static NAME = "downloadAndExtractDbUnitData"

    DownloadAndExtractDbUnitDataDistTask() {
        this.configure {
            group = PLUGIN_GROUP
            def version = ExtensionUtil.getExtension(project).xldIsDataVersion
            project.buildscript.dependencies.add(
                    SERVER_DATA_DIST,
                    "com.xebialabs.deployit.plugins:xld-is-data:${version}:repository@zip"
            )
            from { project.zipTree(project.buildscript.configurations.getByName(SERVER_DATA_DIST).singleFile) }
            into { project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString() }
        }
    }
}
