package com.xebialabs.gradle.integration.util

import org.gradle.api.GradleException
import org.gradle.api.Project

import java.util.concurrent.TimeUnit

class ShutdownUtil {
    private static void waitForShutdown(Project project) {
        def extension = ExtensionsUtil.getExtension(project)

        int triesLeft = extension.serverPingTotalTries
        boolean success = false
        while (triesLeft > 0 && !success) {
            try {
                def http = HTTPUtil.buildRequest("http://localhost:${extension.serverHttpPort}${extension.serverContextRoot}")
                http.handler.failure = {
                    project.logger.info("XL Deploy server successfully shutdown")
                    success = true
                }
                http.post([:]) { resp, reader ->
                    println("Waiting ${extension.serverPingRetrySleepTime} seconds for shutdown. ($triesLeft)")
                    TimeUnit.SECONDS.sleep(extension.serverPingRetrySleepTime)
                }
            } catch (ignored) {
                project.logger.info("XL Deploy server successfully shutdown.")
                success = true
                break
            }
        }
        if (!success) {
            throw new GradleException("Server failed to stop")
        }
    }

    static void shutdownServer(Project project) {
        def extension = ExtensionsUtil.getExtension(project)
        try {
            project.logger.lifecycle("Trying to shutdown integration server at port ${extension.serverHttpPort}")
            def http = HTTPUtil.buildRequest("http://localhost:${extension.serverHttpPort}/deployit/server/shutdown")
            http.post([:]) { resp, reader ->
                waitForShutdown(project)
                project.logger.lifecycle("Integration server at port ${extension.serverHttpPort} is now shutdown")
            }
        } catch (ignored) {
            project.logger.lifecycle("Integration server at port ${extension.serverHttpPort} is not running")
        }
    }
}
