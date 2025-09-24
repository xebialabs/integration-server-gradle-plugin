package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.constant.ServerConstants
import ai.digital.integration.server.common.util.HTTPUtil.Companion.buildRequest
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.File
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class WaitForBootUtil {

    companion object {

        fun byPort(
            project: Project,
            name: String,
            url: String,
            pingRetrySleepTime: Int = ServerConstants.DEFAULT_PING_RETRY_SLEEP_TIME,
            pingTotalTries: Int = ServerConstants.DEFAULT_PING_TOTAL_TRIES
        ) {
            byPort(project, name, url, null, pingRetrySleepTime, pingTotalTries)
        }

        private fun waitForNext(
            project: Project,
            process: Process?,
            triesLeft: Int,
            success: Boolean,
            pingRetrySleepTime: Int = ServerConstants.DEFAULT_PING_RETRY_SLEEP_TIME
        ): Int {
            if (!success) {
                project.logger.lifecycle("Retrying after $pingRetrySleepTime second(s). ($triesLeft)")
                if (process != null) {
                    if (process.waitFor(pingRetrySleepTime.toLong(), TimeUnit.SECONDS)) {
                        return -1
                    }
                } else {
                    TimeUnit.SECONDS.sleep(pingRetrySleepTime.toLong())
                }
            }
            return triesLeft - 1
        }

        fun byFile(
            project: Project,
            process: Process?,
            file: File,
            pingRetrySleepTime: Int = ServerConstants.DEFAULT_PING_RETRY_SLEEP_TIME,
            pingTotalTries: Int = ServerConstants.DEFAULT_PING_TOTAL_TRIES
        ) {
            project.logger.lifecycle("Waiting for $file to be created.")

            var triesLeft = pingTotalTries
            var success = false
            while (triesLeft > 0 && !success) {
                if (file.exists()) {
                    success = true
                }
                triesLeft = waitForNext(project, process, triesLeft, success, pingRetrySleepTime)
            }

            if (!success) {
                throw GradleException("$file has failed to be created.")
            }
        }

        fun byPort(
            project: Project, name: String, url: String, process: Process?,
            pingRetrySleepTime: Int = ServerConstants.DEFAULT_PING_RETRY_SLEEP_TIME,
            pingTotalTries: Int = ServerConstants.DEFAULT_PING_TOTAL_TRIES,
            callback: (LocalDateTime) -> LocalDateTime = { LocalDateTime.now().minusDays(1) }
        ): LocalDateTime {
            project.logger.lifecycle("Waiting for $name to start on URL: $url.")
            var triesLeft = pingTotalTries
            var success = false
            var lastTry = LocalDateTime.now().minusDays(1)
            val client = java.net.http.HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(10))
                .build()
            while (triesLeft > 0 && !success) {
                try {
                    val request = buildRequest(url).GET().build()
                    val response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                    if (response.statusCode() in 200..399) {
                        success = true
                    } else {
                        lastTry = callback(lastTry)
                    }
                } catch (ignored: Exception) {
                    lastTry = callback(lastTry)
                }
                triesLeft = waitForNext(project, process, triesLeft, success, pingRetrySleepTime)
            }
            if (!success) {
                throw GradleException("$name failed to start.")
            }
            return lastTry
        }

        fun byLog(
            project: Project,
            name: String,
            logFile: File,
            containsLine: String,
            process: Process?,
            pingRetrySleepTime: Int = ServerConstants.DEFAULT_PING_RETRY_SLEEP_TIME,
            pingTotalTries: Int = ServerConstants.DEFAULT_PING_TOTAL_TRIES
        ) {
            project.logger.lifecycle("Waiting for $name to start with log: '$containsLine'.")
            var triesLeft = pingTotalTries
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
                triesLeft = waitForNext(project, process, triesLeft, success, pingRetrySleepTime)
            }
            if (!success) {
                throw GradleException("$name failed to start.")
            }
        }
    }
}
