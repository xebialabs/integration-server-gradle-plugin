package ai.digital.integration.server.util


import org.gradle.api.GradleException
import org.gradle.api.Project

import java.util.concurrent.TimeUnit

class ShutdownUtil {
    private static void waitForShutdown(Project project) {
        def server = DeployServerUtil.getServer(project)
        int triesLeft = server.pingTotalTries

        boolean success = false
        while (triesLeft > 0 && !success) {
            try {
                def http = HTTPUtil.buildRequest("${ServerUtil.getUrl(project)}${server.contextRoot}")
                http.handler.failure = {
                    project.logger.lifecycle("XL Deploy server successfully shutdown")
                    success = true
                }
                http.post([:]) { resp, reader ->
                    project.logger.lifecycle("Waiting ${server.pingRetrySleepTime} seconds for shutdown. ($triesLeft)")
                    TimeUnit.SECONDS.sleep(server.pingRetrySleepTime)
                }
            } catch (ignored) {
                project.logger.lifecycle("XL Deploy server successfully shutdown.")
                success = true
                break
            }
        }
        if (!success) {
            throw new GradleException("Server failed to stop")
        }
    }

    static void shutdownServer(Project project) {
        def server = DeployServerUtil.getServer(project)
        try {
            def port = server.httpPort
            project.logger.lifecycle("Trying to shutdown integration server on port ${port}")
            def http = HTTPUtil.buildRequest("${ServerUtil.getUrl(project)}/deployit/server/shutdown")

            http.post([:]) { resp, reader ->
                waitForShutdown(project)
                project.logger.lifecycle("Integration server at port ${port} is now shutdown")
            }
        } catch (ignored) {
            project.logger.lifecycle("Integration server on port ${server.httpPort} is not running")
        }
    }
}
