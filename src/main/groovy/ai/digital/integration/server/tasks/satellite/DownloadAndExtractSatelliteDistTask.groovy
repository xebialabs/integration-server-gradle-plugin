package ai.digital.integration.server.tasks.satellite

import ai.digital.integration.server.domain.Satellite
import ai.digital.integration.server.util.SatelliteUtil
import ai.digital.integration.server.util.ServerUtil
import org.gradle.api.Action
import org.gradle.api.tasks.Copy

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import static ai.digital.integration.server.util.ConfigurationsUtil.SATELLITE_DIST

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

                def taskName = "downloadAndExtractSatellite${satellite.name}"
                def task = project.getTasks().register(taskName, Copy.class, new Action<Copy>() {
                    @Override
                    void execute(Copy copy) {
                        copy.from { project.zipTree(project.buildscript.configurations.getByName(SATELLITE_DIST).singleFile) }
                        copy.into { ServerUtil.getRelativePathInIntegrationServerDist(project, satellite.name) }
                    }
                })
                this.dependsOn task
            }
        }
    }
}
