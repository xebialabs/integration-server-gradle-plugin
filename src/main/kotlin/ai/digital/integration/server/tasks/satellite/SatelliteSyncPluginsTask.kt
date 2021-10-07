package ai.digital.integration.server.tasks.satellite

import ai.digital.integration.server.constant.PluginConstant
import ai.digital.integration.server.util.DeployServerUtil
import ai.digital.integration.server.util.FileUtil
import ai.digital.integration.server.util.SatelliteUtil
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.nio.file.Paths

abstract class SatelliteSyncPluginsTask : DefaultTask() {

    init {
        this.group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(DownloadAndExtractSatelliteDistTask.NAME)
    }

    @TaskAction
    fun launch() {
        SatelliteUtil.getSatellites(project).forEach { satellite ->
            if (satellite.syncPlugins) {
                project.logger.lifecycle("Synchronising plugins for satellite ${satellite.name}")

                // delete plugins from zip
                FileUtils.deleteDirectory(Paths.get(SatelliteUtil.getSatelliteWorkingDir(project, satellite), "plugins")
                    .toFile())

                FileUtil.copyDirs(
                    DeployServerUtil.getServerWorkingDir(project),
                    SatelliteUtil.getSatelliteWorkingDir(project, satellite),
                    listOf(
                        "ext",
                        "hotfix",
                        "plugins"
                    )
                )
            }
        }
    }

    companion object {
        @JvmStatic
        val NAME = "satelliteSyncPlugins"
    }
}
