package ai.digital.integration.server.tasks.satellite

import ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.util.ConfigurationsUtil.Companion.SATELLITE_DIST
import ai.digital.integration.server.util.DeployServerUtil
import ai.digital.integration.server.util.SatelliteUtil
import org.gradle.api.tasks.Copy

abstract class DownloadAndExtractSatelliteDistTask : Copy() {

    init {
        this.group = PLUGIN_GROUP

        SatelliteUtil.getSatellites(project).forEach { satellite ->
            project.buildscript.dependencies.add(
                SATELLITE_DIST,
                "com.xebialabs.xl-platform.satellite:xl-satellite-server:${satellite.version}@zip"
            )

            val taskName = "downloadAndExtractSatellite${satellite.name}"
            val task = project.tasks.register(taskName, Copy::class.java) { copy ->
                copy.from(project.zipTree(project.buildscript.configurations.getByName(SATELLITE_DIST).singleFile))
                copy.into(DeployServerUtil.getRelativePathInIntegrationServerDist(project, satellite.name))
            }
            this.dependsOn(task)
        }
    }

    companion object {
        @JvmStatic
        val NAME = "downloadAndExtractSatelliteServer"
    }
}
