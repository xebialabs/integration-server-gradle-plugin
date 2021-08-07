package com.xebialabs.gradle.integration.tasks.satellite

import com.xebialabs.gradle.integration.domain.Satellite
import com.xebialabs.gradle.integration.util.EnvironmentUtil
import com.xebialabs.gradle.integration.util.ExtensionUtil
import com.xebialabs.gradle.integration.util.LocationUtil
import com.xebialabs.gradle.integration.util.ProcessUtil
import com.xebialabs.gradle.integration.util.WaitForBootUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

import static com.xebialabs.gradle.integration.constant.PluginConstant.PLUGIN_GROUP

class StartSatelliteTask extends DefaultTask {
    static NAME = "startSatellite"

    StartSatelliteTask() {

        def dependencies = [
                DownloadAndExtractSatelliteDistTask.NAME,
                CopySatelliteOverlaysTask.NAME
        ]
        this.configure {
            group = PLUGIN_GROUP
            dependsOn(dependencies)
        }
    }

    private def getBinDir(Satellite satellite) {
        Paths.get(LocationUtil.getSatelliteWorkingDir(project), "bin").toFile()
    }

    private void startSatellite(Satellite satellite) {
        project.logger.lifecycle("Launching Satellite ${satellite.name}.")

        ProcessUtil.exec([
                command    : "run",
                environment: EnvironmentUtil.getEnv(
                        "SATELLITE_OPTS",
                        satellite.debugSuspend,
                        satellite.debugPort,
                        "xl-satellite.log"
                ),
                workDir    : getBinDir()
        ])
        project.logger.lifecycle("Satellite successfully started.")
    }

    private def getSatelliteLog() {
        project.file("${LocationUtil.getSatelliteWorkingDir(project)}/log/xl-satellite.log")
    }

    @TaskAction
    void launch() {
        ExtensionUtil.getExtension(project).satellites.each { Satellite satellite ->
            startSatellite(satellite)
            WaitForBootUtil.byLog(project, "Satellite", getSatelliteLog(), "XL Satellite has started")
        }
    }
}
