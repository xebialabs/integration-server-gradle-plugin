package ai.digital.integration.server.deploy.tasks.satellite

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.IntegrationServerUtil
import ai.digital.integration.server.deploy.internals.DeployConfigurationsUtil.Companion.SATELLITE_DIST
import ai.digital.integration.server.deploy.internals.SatelliteUtil
import org.gradle.api.tasks.Copy

open class DownloadAndExtractSatelliteDistTask : Copy() {

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
                copy.into(IntegrationServerUtil.getRelativePathInIntegrationServerDist(project, satellite.name))
            }
            this.dependsOn(task)
        }
    }

    companion object {
        const val NAME = "downloadAndExtractSatelliteServer"
    }
}
