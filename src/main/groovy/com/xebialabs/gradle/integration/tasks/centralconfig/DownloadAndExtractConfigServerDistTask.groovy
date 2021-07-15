package com.xebialabs.gradle.integration.tasks.centralconfig

import com.xebialabs.gradle.integration.util.ExtensionsUtil
import org.gradle.api.tasks.Copy

import static com.xebialabs.gradle.integration.util.ConfigurationsUtil.CENTRAL_CONFIG_DATA_DIST
import static com.xebialabs.gradle.integration.util.PluginUtil.DIST_DESTINATION_NAME
import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class DownloadAndExtractConfigServerDistTask extends Copy {
    static NAME = "DownloadAndExtractCentralConfigServer"

    DownloadAndExtractConfigServerDistTask() {
        this.configure {
            group = PLUGIN_GROUP
            def configServerVersion = ExtensionsUtil.getExtension(project).configServerVersion
            project.buildscript.dependencies.add(
                    CENTRAL_CONFIG_DATA_DIST,
                    "ai.digital.config:central-configuration-server:$configServerVersion:server@zip"
            )
            from { project.zipTree(project.buildscript.configurations.getByName(CENTRAL_CONFIG_DATA_DIST).singleFile) }
            into { project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString() }
        }
    }
}
