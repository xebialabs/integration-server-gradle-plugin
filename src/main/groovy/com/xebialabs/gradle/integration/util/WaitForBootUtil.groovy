package com.xebialabs.gradle.integration.util

import org.gradle.api.GradleException
import org.gradle.api.Project

import java.util.concurrent.TimeUnit

class WaitForBootUtil {

    static void byPort(Project project, String name, String url, Integer port) {
        project.logger.lifecycle("Waiting for $name to start.")

        def server = ServerUtil.getServer(project)
        int triesLeft = server.pingTotalTries
        boolean success = false
        while (triesLeft > 0 && !success) {
            try {
                def http = HTTPUtil.buildRequest(url)
                http.get([:]) { resp, reader ->
                    println("$name successfully started on port $port.")
                    success = true
                }
            } catch (ignored) {
            }
            if (!success) {
                println("Retrying after ${server.pingRetrySleepTime} second(s). ($triesLeft)")
                TimeUnit.SECONDS.sleep(server.pingRetrySleepTime)
                triesLeft -= 1
            }
        }
        if (!success) {
            throw new GradleException("$name failed to start.")
        }
    }

    static def byLog(Project project, String name, File logFile, String containsLine) {
        project.logger.lifecycle("Waiting for $name to start.")
        def server = ServerUtil.getServer(project)
        int triesLeft = server.pingTotalTries
        boolean success = false

        while (triesLeft > 0 && !success) {
            try {
                logFile.readLines().each { String line ->
                    if (line.contains(containsLine)) {
                        println("$name successfully started.")
                        success = true
                    }
                }
            } catch (ignored) {
            }
            if (!success) {
                println("Retrying after ${server.pingRetrySleepTime} second(s). ($triesLeft)")
                TimeUnit.SECONDS.sleep(server.pingRetrySleepTime)
                triesLeft -= 1
            }
        }
        if (!success) {
            throw new GradleException("$name failed to start.")
        }
    }
}
