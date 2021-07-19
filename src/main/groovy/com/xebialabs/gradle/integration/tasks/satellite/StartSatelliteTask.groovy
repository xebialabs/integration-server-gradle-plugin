package com.xebialabs.gradle.integration.tasks.satellite

import com.xebialabs.gradle.integration.util.ExtensionsUtil
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
        ProcessUtil.exec([
                command    : "run",
                environment: getEnv(),
                workDir    : getBinDir()
        ])
        project.logger.lifecycle("Satellite Server successfully started")
    }

    def waitForBoot() {
        project.logger.lifecycle("Waiting for xl satellite to start")
        def extension = ExtensionsUtil.getExtension(project)
        int triesLeft = extension.serverPingTotalTries
        boolean success = false

        def runtimeDir = ExtensionsUtil.getSatelliteWorkingDir(project)
        def workerLog = project.file("$runtimeDir/log/xl-satellite.log")

        while (triesLeft > 0 && !success) {
            try {
                workerLog.readLines().each { String line ->
                    if (line.contains("XL Satellite has started")) {
                        println("XL Satellite Worker successfully started.")
                        success = true
                    }
                }
            } catch (ignored) {}
            if (!success) {
                println("Waiting  ${extension.serverPingRetrySleepTime} second(s) for satellite startup. ($triesLeft)")
                TimeUnit.SECONDS.sleep(extension.serverPingRetrySleepTime)
                triesLeft -= 1
            }
        }
        if (!success) {
            throw new GradleException("Satellite failed to start")
        }
    }

    @TaskAction
    void launch() {
        startServer()
        waitForBoot()
    }
}