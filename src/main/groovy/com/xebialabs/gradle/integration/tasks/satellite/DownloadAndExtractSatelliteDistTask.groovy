package com.xebialabs.gradle.integration.tasks.satellite

import com.xebialabs.gradle.integration.util.ExtensionsUtil
import org.gradle.api.tasks.Copy

import static com.xebialabs.gradle.integration.util.ConfigurationsUtil.SATELLITE_DATA_DIST
import static com.xebialabs.gradle.integration.util.PluginUtil.DIST_DESTINATION_NAME
import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class DownloadAndExtractSatelliteDistTask extends Copy {
    static NAME = "downloadAndExtractSatelliteServer"

    DownloadAndExtractSatelliteDistTask() {
        this.configure {
            group = PLUGIN_GROUP
            def satelliteVersion = ExtensionsUtil.getExtension(project).satelliteVersion
            project.buildscript.dependencies.add(
                    SATELLITE_DATA_DIST,
                    "com.xebialabs.xl-platform.satellite:xl-satellite-server:$satelliteVersion@zip"
            )
            from { project.zipTree(project.buildscript.configurations.getByName(SATELLITE_DATA_DIST).singleFile) }
            into { project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString() }
        }
    }
}
