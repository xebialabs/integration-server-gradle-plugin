package ai.digital.integration.server.tasks.satellite

import ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.util.OverlaysUtil
import ai.digital.integration.server.util.SatelliteUtil
import org.gradle.api.DefaultTask

abstract class SatelliteOverlaysTask : DefaultTask() {

    companion object {
        @JvmStatic
        val NAME = "satelliteOverlays"

        @JvmStatic
        val PREFIX = "satellite"
    }

    init {
        this.group = PLUGIN_GROUP
        this.mustRunAfter(DownloadAndExtractSatelliteDistTask.NAME)

        project.afterEvaluate {
            SatelliteUtil.getSatellites(project).forEach { satellite ->
                satellite.overlays.forEach { overlay ->
                    OverlaysUtil.defineOverlay(project, this,
                        SatelliteUtil.getSatelliteWorkingDir(project, satellite),
                        PREFIX,
                        overlay,
                        listOf("downloadAndExtractSatellite${satellite.name}")
                    )
                }
            }
        }
    }
}