package com.xebialabs.gradle.integration.util


import org.gradle.api.GradleException
import org.gradle.api.Project

import java.util.concurrent.TimeUnit

class ShutdownUtil {
    private static void waitForShutdown(Project project) {
        def server = ServerUtil.getServer(project)
        int triesLeft = server.pingTotalTries

        boolean success = false
        while (triesLeft > 0 && !success) {
            try {
                def http = HTTPUtil.buildRequest("http://localhost:${server.httpPort}${server.contextRoot}")
                http.handler.failure = {
                    project.logger.info("XL Deploy server successfully shutdown")
                    success = true
                }
                http.post([:]) { resp, reader ->
                    println("Waiting ${server.pingRetrySleepTime} seconds for shutdown. ($triesLeft)")
                    TimeUnit.SECONDS.sleep(server.pingRetrySleepTime)
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
        def server = ServerUtil.getServer(project)
        try {
            def port = server.httpPort
            project.logger.lifecycle("Trying to shutdown integration server on port ${port}")
            def http = HTTPUtil.buildRequest("http://localhost:$port}/deployit/server/shutdown")

            http.post([:]) { resp, reader ->
                waitForShutdown(project)
                project.logger.lifecycle("Integration server at port ${port} is now shutdown")
            }
        } catch (ignored) {
            project.logger.lifecycle("Integration server on port ${server.httpPort} is not running")
        }
    }
}
