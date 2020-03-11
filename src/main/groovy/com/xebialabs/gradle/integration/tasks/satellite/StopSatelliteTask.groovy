package com.xebialabs.gradle.integration.tasks.satellite

import com.xebialabs.gradle.integration.tasks.DownloadAndExtractSatelliteDistTask
import com.xebialabs.gradle.integration.util.DbUtil
import com.xebialabs.gradle.integration.util.FileUtil
import com.xebialabs.gradle.integration.util.ProcessUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP
import static com.xebialabs.gradle.integration.util.PluginUtil.getDistLocation

class StopSatelliteTask extends DefaultTask {
    static NAME = "stopSatellite"
    static STOP_SATELLITE_SCRIPT = "stopSatellite.sh"


    StopSatelliteTask() {
        group = PLUGIN_GROUP
    }

    private def copyStopSatelliteScript() {
        def from = StopSatelliteTask.class.classLoader.getResourceAsStream("satellite/bin/$STOP_SATELLITE_SCRIPT")
        def intoDir = getDistLocation(project).resolve(STOP_SATELLITE_SCRIPT)
        FileUtil.copyFile(from, intoDir)
    }

    private def getWorkingDir() {
        return getDistLocation(project).toFile()
    }

    private void stopSatellite() {
        project.logger.lifecycle("Stopping satellite")

        ProcessUtil.chMod(project, "777", getDistLocation(project).resolve(STOP_SATELLITE_SCRIPT).toAbsolutePath().toString())
        ProcessUtil.exec([
                command: "stopSatellite",
                workDir: getWorkingDir()
        ])
    }

    @TaskAction
    void stop() {
        copyStopSatelliteScript()
        stopSatellite()
    }
}
