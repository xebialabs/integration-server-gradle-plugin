package ai.digital.integration.server.tasks.satellite

import ai.digital.integration.server.domain.Satellite
import ai.digital.integration.server.util.FileUtil
import ai.digital.integration.server.util.ProcessUtil
import ai.digital.integration.server.util.SatelliteUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class ShutdownSatelliteTask extends DefaultTask {
    public static String NAME = "shutdownSatellite"
    public static String STOP_SATELLITE_SCRIPT = "stopSatellite.sh"

    ShutdownSatelliteTask() {
        group = PLUGIN_GROUP
    }

    def copyStopSatelliteScript(Satellite satellite) {
        def from = ShutdownSatelliteTask.class.classLoader.getResourceAsStream("satellite/bin/$STOP_SATELLITE_SCRIPT")
        def intoDir = Paths.get(SatelliteUtil.getSatelliteWorkingDir(project, satellite)).resolve(STOP_SATELLITE_SCRIPT)
        FileUtil.copyFile(from, intoDir)
    }

    void stopSatellite(Satellite satellite) {
        ProcessUtil.chMod(project, "777", Paths.get(SatelliteUtil.getSatelliteWorkingDir(project, satellite))
                .resolve(STOP_SATELLITE_SCRIPT).toAbsolutePath().toString())
        ProcessUtil.exec([
                command: "stopSatellite",
                workDir: new File(SatelliteUtil.getSatelliteWorkingDir(project, satellite))
        ])
        project.logger.lifecycle("Satellite server '${satellite.name}' successfully shutdown.")
    }


    @TaskAction
    void stop() {
        SatelliteUtil.getSatellites(project).each { Satellite satellite ->
            project.logger.lifecycle("Shutting down satellite ${satellite.name}")
            copyStopSatelliteScript(satellite)
            stopSatellite(satellite)
        }
    }
}
