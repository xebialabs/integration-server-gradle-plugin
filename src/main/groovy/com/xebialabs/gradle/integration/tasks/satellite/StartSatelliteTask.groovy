package com.xebialabs.gradle.integration.tasks.satellite

import com.typesafe.config.ConfigRenderOptions
import com.xebialabs.gradle.integration.tasks.CopyOverlaysTask
import com.xebialabs.gradle.integration.util.ApplicationsUtil
import com.xebialabs.gradle.integration.util.ExtensionsUtil
import com.xebialabs.gradle.integration.util.FileUtil
import com.xebialabs.gradle.integration.util.HTTPUtil
import com.xebialabs.gradle.integration.util.PluginUtil
import com.xebialabs.gradle.integration.util.ProcessUtil
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

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
        Long pid = ProcessUtil.exec([
                command    : "run",
                environment: getEnv(),
                workDir    : getBinDir()
        ])

      /*  project.logger.lifecycle("current process id:"+ProcessHandle.current());
        project.logger.lifecycle("process id:"+pid);
        project.logger.lifecycle("parent process id:"+ProcessHandle.of(pid).get().parent().get());*/


        writeSatellitePidToFile(pid)
    }

    private def writeSatellitePidToFile(Long pid) {
        def targetDir = project.buildDir.toPath().resolve(PluginUtil.DIST_DESTINATION_NAME).toAbsolutePath().toString()
        new File("${targetDir}/${ApplicationsUtil.SATELLITE_START}").text = pid
    }

    @TaskAction
    void launch() {
        startServer()
    }
}