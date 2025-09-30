package ai.digital.integration.server.deploy.tasks.satellite

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.IntegrationServerUtil
import ai.digital.integration.server.deploy.internals.DeployConfigurationsUtil.Companion.SATELLITE_DIST
import ai.digital.integration.server.deploy.internals.SatelliteUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Copy

open class DownloadAndExtractSatelliteDistTask : DefaultTask() {

    init {
        this.group = PLUGIN_GROUP

        SatelliteUtil.getSatellites(project).forEach { satellite ->
            project.dependencies.add(
                SATELLITE_DIST,
                "com.xebialabs.xl-platform.satellite:xl-satellite-server:${satellite.version}@zip"
            )

            val taskName = "downloadAndExtractSatellite${satellite.name}"
            this.dependsOn(project.tasks.register(taskName, Copy::class.java) {
                from(project.zipTree(project.configurations.getByName(SATELLITE_DIST).singleFile))
                into(IntegrationServerUtil.getRelativePathInIntegrationServerDist(project, satellite.name))
            })
        }
    }

    companion object {
        const val NAME = "downloadAndExtractSatelliteServer"
    }
}
