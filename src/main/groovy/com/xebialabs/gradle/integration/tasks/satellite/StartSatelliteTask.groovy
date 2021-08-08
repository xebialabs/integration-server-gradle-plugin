package com.xebialabs.gradle.integration.tasks.satellite

import com.xebialabs.gradle.integration.domain.Satellite
import com.xebialabs.gradle.integration.util.EnvironmentUtil
import com.xebialabs.gradle.integration.util.ProcessUtil
import com.xebialabs.gradle.integration.util.SatelliteUtil
import com.xebialabs.gradle.integration.util.WaitForBootUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

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

    @TaskAction
    void launch() {
        SatelliteUtil.getSatellites(project).each { Satellite satellite ->
            def binDir = SatelliteUtil.getBinDir(project, satellite)
            project.logger.lifecycle("Launching Satellite '${satellite.name} from ${binDir}'.")
            ProcessUtil.exec([
                    command    : "run",
                    environment: EnvironmentUtil.getEnv(
                            "SATELLITE_OPTS",
                            satellite.debugSuspend,
                            satellite.debugPort,
                            "xl-satellite.log"
                    ),
                    workDir    : binDir
            ])
            project.logger.lifecycle("Satellite '${satellite.name}' successfully started.")
            WaitForBootUtil.byLog(project, "Satellite ${satellite.name}", SatelliteUtil.getSatelliteLog(project, satellite), "XL Satellite has started")
        }
    }
}
