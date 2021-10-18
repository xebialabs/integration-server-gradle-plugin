package ai.digital.integration.server.deploy.tasks.satellite

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.deploy.domain.Satellite
import ai.digital.integration.server.common.util.FileUtil
import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.deploy.util.SatelliteUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Paths

abstract class ShutdownSatelliteTask : DefaultTask() {

    init {
        this.group = PLUGIN_GROUP
    }

    private fun copyStopSatelliteScript(satellite: Satellite) {
        val from = {}::class.java.classLoader.getResourceAsStream("satellite/bin/$STOP_SATELLITE_SCRIPT")
        val intoDir = Paths.get(SatelliteUtil.getSatelliteWorkingDir(project, satellite)).resolve(STOP_SATELLITE_SCRIPT)

        from?.let { source ->
            FileUtil.copyFile(source, intoDir)
        }
    }

    private fun stopSatellite(satellite: Satellite) {
        ProcessUtil.chMod(project, "777", Paths.get(SatelliteUtil.getSatelliteWorkingDir(project, satellite))
            .resolve(STOP_SATELLITE_SCRIPT).toAbsolutePath().toString())
        ProcessUtil.exec(mapOf<String, Any?>(
            "command" to "stopSatellite",
            "workDir" to File(SatelliteUtil.getSatelliteWorkingDir(project, satellite))
        ))
        project.logger.lifecycle("Satellite server '${satellite.name}' successfully shutdown.")
    }


    @TaskAction
    fun stop() {
        SatelliteUtil.getSatellites(project).forEach { satellite ->
            project.logger.lifecycle("Shutting down satellite ${satellite.name}")
            copyStopSatelliteScript(satellite)
            stopSatellite(satellite)
        }
    }

    companion object {
        @JvmStatic
        val NAME = "shutdownSatellite"

        @JvmStatic
        val STOP_SATELLITE_SCRIPT = "stopSatellite.sh"
    }
}
