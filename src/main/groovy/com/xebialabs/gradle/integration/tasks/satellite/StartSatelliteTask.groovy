package com.xebialabs.gradle.integration.tasks.satellite

import com.xebialabs.gradle.integration.util.EnvironmentUtil
import com.xebialabs.gradle.integration.util.ExtensionsUtil
import com.xebialabs.gradle.integration.util.ProcessUtil
import com.xebialabs.gradle.integration.util.WaitForBootUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

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

    private def getBinDir() {
        Paths.get(ExtensionsUtil.getSatelliteWorkingDir(project), "bin").toFile()
    }

    private void startServer() {
        project.logger.lifecycle("Launching Satellite.")
        ProcessUtil.exec([
                command    : "run",
                environment: EnvironmentUtil.getEnv(project, "SATELLITE_OPTS"),
                workDir    : getBinDir()
        ])
        project.logger.lifecycle("Satellite successfully started.")
    }

    private def getWorkerLog() {
        project.file("${ExtensionsUtil.getSatelliteWorkingDir(project)}/log/xl-satellite.log")
    }

    @TaskAction
    void launch() {
        startServer()
        WaitForBootUtil.byLog(project, "Satellite", getWorkerLog(), "XL Satellite has started")
    }
}
