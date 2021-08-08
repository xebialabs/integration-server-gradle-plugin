package com.xebialabs.gradle.integration.tasks.satellite

import com.xebialabs.gradle.integration.domain.Satellite
import com.xebialabs.gradle.integration.util.FileUtil
import com.xebialabs.gradle.integration.util.ProcessUtil
import com.xebialabs.gradle.integration.util.SatelliteUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

import static com.xebialabs.gradle.integration.constant.PluginConstant.PLUGIN_GROUP

class ShutdownSatelliteTask extends DefaultTask {
    static NAME = "shutDownSatellite"
    static STOP_SATELLITE_SCRIPT = "stopSatellite.sh"

    ShutdownSatelliteTask() {
        group = PLUGIN_GROUP
    }

    private def copyStopSatelliteScript(Satellite satellite) {
        def from = ShutdownSatelliteTask.class.classLoader.getResourceAsStream("satellite/bin/$STOP_SATELLITE_SCRIPT")
        def intoDir = Paths.get(SatelliteUtil.getSatelliteWorkingDir(project, satellite)).resolve(STOP_SATELLITE_SCRIPT)
        FileUtil.copyFile(from, intoDir)
    }

    private void stopSatellite(Satellite satellite) {
        ProcessUtil.chMod(project, "777", Paths.get(SatelliteUtil.getSatelliteWorkingDir(project, satellite))
                .resolve(STOP_SATELLITE_SCRIPT).toAbsolutePath().toString())
        ProcessUtil.exec([
                command: "stopSatellite",
                workDir: SatelliteUtil.getSatelliteWorkingDir(project, satellite)
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
