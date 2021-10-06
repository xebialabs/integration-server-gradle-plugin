package ai.digital.integration.server.util

import ai.digital.integration.server.domain.Server
import ai.digital.integration.server.util.DeployServerUtil.Companion.getServer
import ai.digital.integration.server.util.HTTPUtil.Companion.buildRequest
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.File
import java.util.concurrent.TimeUnit

class WaitForBootUtil {

    companion object {

        @JvmStatic
        fun byPort(project: Project, name: String, url: String?) {
            byPort(project, name, url, null)
        }

        private fun waitForNext(project: Project, process: Process?, server: Server, triesLeft: Int, success: Boolean): Int {
            if (!success) {
                project.logger.lifecycle("Retrying after " + server.pingRetrySleepTime.toString() + " second(s). (" + triesLeft.toString() + ")")
                if (process != null) {
                    if (process.waitFor(server.pingRetrySleepTime.toLong(), TimeUnit.SECONDS)) {
                        return -1
                    }
                } else {
                    TimeUnit.SECONDS.sleep(server.pingRetrySleepTime.toLong())
                }
            }
            return triesLeft - 1
        }

        @JvmStatic
        fun byPort(project: Project, name: String, url: String?, process: Process?) {
            project.logger.lifecycle("Waiting for $name to start.")
            val server = getServer(project)
            var triesLeft = server.pingTotalTries
            var success = false
            while (triesLeft > 0 && !success) {
                try {
                    val http = buildRequest(url!!)
                    http.get(mutableMapOf<String, Any>())
                    success = true
                } catch (ignored: Exception) {
                }
                triesLeft = waitForNext(project, process, server, triesLeft, success)
            }
            if (!success) {
                throw GradleException("$name failed to start.")
            }
        }

        @JvmStatic
        fun byLog(project: Project, name: String, logFile: File, containsLine: String, process: Process?) {
            project.logger.lifecycle("Waiting for $name to start.")
            val server = getServer(project)
            var triesLeft = server.pingTotalTries
            var success = false
            while (triesLeft > 0 && !success) {
                try {
                    logFile.forEachLine { line ->
                        if (line.contains(containsLine)) {
                            project.logger.lifecycle("$name successfully started.")
                            success = true
                        }
                    }
                } catch (ignored: Exception) {
                }
                triesLeft = waitForNext(project, process, server, triesLeft, success)
            }
            if (!success) {
                throw GradleException("$name failed to start.")
            }
        }
    }
}
