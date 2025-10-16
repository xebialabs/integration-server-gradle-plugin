package ai.digital.integration.server.deploy.tasks.satellite

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.OverlaysUtil
import ai.digital.integration.server.deploy.internals.SatelliteUtil
import org.gradle.api.DefaultTask

open class SatelliteOverlaysTask : DefaultTask() {

    companion object {
        const val NAME = "satelliteOverlays"
        const val PREFIX = "satellite"
    }

    init {
        this.group = PLUGIN_GROUP
        this.mustRunAfter(DownloadAndExtractSatelliteDistTask.NAME)
        val currentTask = this

        val configureOverlays = {
            SatelliteUtil.getSatellites(project).forEach { satellite ->
                satellite.overlays.forEach { overlay ->
                    OverlaysUtil.defineOverlay(project, currentTask,
                        SatelliteUtil.getSatelliteWorkingDir(project, satellite),
                        PREFIX,
                        overlay,
                        listOf("downloadAndExtractSatellite${satellite.name}")
                    )
                }
            }
        }

        if (project.state.executed) {
            configureOverlays()
        } else {
            project.afterEvaluate {
                configureOverlays()
            }
        }
    }
}
