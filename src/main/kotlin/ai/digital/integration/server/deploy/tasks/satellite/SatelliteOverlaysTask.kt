package ai.digital.integration.server.deploy.tasks.satellite

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.OverlaysUtil
import ai.digital.integration.server.deploy.util.SatelliteUtil
import org.gradle.api.DefaultTask

open class SatelliteOverlaysTask : DefaultTask() {

    companion object {
        const val NAME = "satelliteOverlays"
        const val PREFIX = "satellite"
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
