package ai.digital.integration.server.util

import org.gradle.api.GradleException
import org.gradle.api.Project

import java.util.concurrent.TimeUnit

class WaitForBootUtil {

    static void byPort(Project project, String name, String url, Integer port) {
        byPort(project, name, url, port, null)
    }

    static void byPort(Project project, String name, String url, Integer port, Process process) {
        project.logger.lifecycle("Waiting for $name to start.")

        def server = ServerUtil.getServer(project)
        int triesLeft = server.pingTotalTries
        boolean success = false
        while (triesLeft > 0 && !success) {
            try {
                def http = HTTPUtil.buildRequest(url)
                http.get([:]) { resp, reader ->
                    project.logger.lifecycle("$name successfully started on port $port.")
                    success = true
                }
            } catch (ignored) {
            }
            if (!success) {
                project.logger.lifecycle("Retrying after ${server.pingRetrySleepTime} second(s). ($triesLeft)")
                if (process != null) {
                    if (process.waitFor(server.pingRetrySleepTime, TimeUnit.SECONDS)) {
                        triesLeft = -1
                    }
                } else {
                    TimeUnit.SECONDS.sleep(server.pingRetrySleepTime)
                }
                triesLeft -= 1
            }
        }
        if (!success) {
            throw new GradleException("$name failed to start.")
        }
    }

    static def byLog(Project project, String name, File logFile, String containsLine, Process process) {
        project.logger.lifecycle("Waiting for $name to start.")
        def server = ServerUtil.getServer(project)
        int triesLeft = server.pingTotalTries
        boolean success = false

        while (triesLeft > 0 && !success) {
            try {
                logFile.readLines().each { String line ->
                    if (line.contains(containsLine)) {
                        project.logger.lifecycle("$name successfully started.")
                        success = true
                    }
                }
            } catch (ignored) {
            }
            if (!success) {
                project.logger.lifecycle("Retrying after ${server.pingRetrySleepTime} second(s). ($triesLeft)")
                if (process != null) {
                    if (process.waitFor(server.pingRetrySleepTime, TimeUnit.SECONDS)) {
                        triesLeft = -1
                    }
                } else {
                    TimeUnit.SECONDS.sleep(server.pingRetrySleepTime)
                }
                triesLeft -= 1
            }
        }
        if (!success) {
            throw new GradleException("$name failed to start.")
        }
    }
}
