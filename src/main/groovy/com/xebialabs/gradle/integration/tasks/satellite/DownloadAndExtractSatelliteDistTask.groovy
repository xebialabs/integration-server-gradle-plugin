package com.xebialabs.gradle.integration.tasks.satellite

import com.xebialabs.gradle.integration.domain.Satellite
import com.xebialabs.gradle.integration.util.SatelliteUtil
import org.gradle.api.tasks.Copy

import static com.xebialabs.gradle.integration.constant.PluginConstant.DIST_DESTINATION_NAME
import static com.xebialabs.gradle.integration.constant.PluginConstant.PLUGIN_GROUP
import static com.xebialabs.gradle.integration.util.ConfigurationsUtil.SATELLITE_DIST

class DownloadAndExtractSatelliteDistTask extends Copy {
    static NAME = "downloadAndExtractSatelliteServer"

    DownloadAndExtractSatelliteDistTask() {
        this.configure {
            group = PLUGIN_GROUP

            SatelliteUtil.getSatellites(project).each { Satellite satellite ->
                project.buildscript.dependencies.add(
                        SATELLITE_DIST,
                        "com.xebialabs.xl-platform.satellite:xl-satellite-server:${satellite.version}@zip"
                )
                from { project.zipTree(project.buildscript.configurations.getByName(SATELLITE_DIST).singleFile) }
                into { project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString() }
            }
        }
    }
}
