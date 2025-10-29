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
                    // Check if process is alive before waiting
                    if (!process.isAlive) {
                        val exitCode = process.exitValue()
                        project.logger.error("Process terminated early with exit code: $exitCode")
                        if (process.errorStream != null) {
                            try {
                                val errorOutput = process.errorStream.bufferedReader().readText()
                                if (errorOutput.isNotBlank()) {
                                    project.logger.error("Process error output: $errorOutput")
                                }
                            } catch (e: Exception) {
                                // Ignore
                            }
                        }
                        return -1
                    }
                    // Process is alive, wait for the retry sleep time
                    TimeUnit.SECONDS.sleep(pingRetrySleepTime.toLong())
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
            var lastException: Exception? = null
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
                        project.logger.debug("Received response code: ${response.statusCode()}")
                        lastTry = callback(lastTry)
                    }
                } catch (e: Exception) {
                    lastException = e
                    project.logger.debug("Connection attempt failed: ${e.message}")
                    lastTry = callback(lastTry)
                }
                triesLeft = waitForNext(project, process, triesLeft, success, pingRetrySleepTime)
                if (triesLeft < 0) {
                    // Process died
                    throw GradleException("$name process terminated early. Check logs for details.")
                }
            }
            if (!success) {
                val errorMsg = if (lastException != null) {
                    "$name failed to start. Last error: ${lastException.message}"
                } else {
                    "$name failed to start after $pingTotalTries attempts."
                }
                throw GradleException(errorMsg)
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
                    if (logFile.exists()) {
                        logFile.forEachLine { line ->
                            if (line.contains(containsLine)) {
                                project.logger.lifecycle("$name successfully started.")
                                success = true
                            }
                        }
                    } else {
                        project.logger.debug("Log file does not exist yet: ${logFile.absolutePath}")
                    }
                } catch (e: Exception) {
                    project.logger.debug("Error reading log file: ${e.message}")
                }
                triesLeft = waitForNext(project, process, triesLeft, success, pingRetrySleepTime)
                if (triesLeft < 0) {
                    // Process died - try to show last log lines
                    if (logFile.exists()) {
                        try {
                            val lastLines = logFile.readLines().takeLast(20).joinToString("\n")
                            project.logger.error("$name process terminated. Last log lines:\n$lastLines")
                        } catch (e: Exception) {
                            project.logger.error("$name process terminated. Could not read log file.")
                        }
                    }
                    throw GradleException("$name process terminated early. Check logs at: ${logFile.absolutePath}")
                }
            }
            if (!success) {
                var errorMsg = "$name failed to start."
                if (logFile.exists()) {
                    try {
                        val lastLines = logFile.readLines().takeLast(20).joinToString("\n")
                        errorMsg += "\nLast log lines:\n$lastLines"
                    } catch (e: Exception) {
                        errorMsg += "\nCould not read log file at: ${logFile.absolutePath}"
                    }
                } else {
                    errorMsg += "\nLog file not found: ${logFile.absolutePath}"
                }
                throw GradleException(errorMsg)
            }
        }
    }
}
