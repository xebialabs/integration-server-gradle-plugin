package com.xebialabs.gradle.integration.tasks.satellite

import com.xebialabs.gradle.integration.tasks.DownloadAndExtractSatelliteDistTask
import com.xebialabs.gradle.integration.util.ExtensionsUtil
import com.xebialabs.gradle.integration.util.FileUtil
import com.xebialabs.gradle.integration.util.ProcessUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

import static com.xebialabs.gradle.integration.util.PluginUtil.getPLUGIN_GROUP

class StartSatelliteTask extends DefaultTask {
    static NAME = "startSatellite"

    StartSatelliteTask() {

        def dependencies = [
                DownloadAndExtractSatelliteDistTask.NAME,
        ]
        this.configure {
            group = PLUGIN_GROUP
            dependsOn(dependencies)
        }
    }

    private def getEnv() {
        def extension = ExtensionsUtil.getExtension(project)
        def opts = "-Xmx1024m"
        def suspend = extension.satelliteDebugSuspend ? 'y' : 'n'
        if (extension.satelliteDebugPort) {
            opts = "${opts} -agentlib:jdwp=transport=dt_socket,server=y,suspend=${suspend},address=${extension.satelliteDebugPort}"
        }
        ["SATELLITE_OPTS": opts.toString()]
    }

    private def getBinDir() {
        Paths.get(ExtensionsUtil.getSatelliteWorkingDir(project), "bin").toFile()
    }

    private void startServer() {
        project.logger.lifecycle("Launching satellite")
        ProcessUtil.exec([
                command    : "run",
                environment: getEnv(),
                workDir    : getBinDir()
        ])
    }

    private void writeSatelliteConf() {
        project.logger.lifecycle("Writing satellite.conf file")

        def configStream = StartSatelliteTask.class.classLoader.getResourceAsStream("satellite/conf/satellite.conf")
        def confDest = Paths.get("${ExtensionsUtil.getSatelliteWorkingDir(project)}/conf/satellite.conf")

        FileUtil.copyFile(configStream, confDest)
    }
    @TaskAction
    void launch() {
//        shutdownSatellite(project)
        writeSatelliteConf()
        startServer()
    }
}
