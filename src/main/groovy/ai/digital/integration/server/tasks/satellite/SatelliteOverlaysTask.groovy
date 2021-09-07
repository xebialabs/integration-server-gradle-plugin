package ai.digital.integration.server.tasks.satellite

import ai.digital.integration.server.domain.Satellite
import ai.digital.integration.server.util.OverlaysUtil
import ai.digital.integration.server.util.SatelliteUtil
import org.gradle.api.DefaultTask

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class SatelliteOverlaysTask extends DefaultTask {
    static NAME = "satelliteOverlays"

    static PREFIX = "satellite"

    SatelliteOverlaysTask() {
        this.configure { ->
            group = PLUGIN_GROUP
            mustRunAfter DownloadAndExtractSatelliteDistTask.NAME

            project.afterEvaluate {
                SatelliteUtil.getSatellites(project).each { Satellite satellite ->
                    satellite.overlays.each { Map.Entry<String, List<Object>> overlay ->
                        OverlaysUtil.defineOverlay(project, this, SatelliteUtil.getSatelliteWorkingDir(project, satellite), PREFIX, overlay, ["downloadAndExtractSatellite${satellite.name}"])
                    }
                }
            }
        }
    }
}
