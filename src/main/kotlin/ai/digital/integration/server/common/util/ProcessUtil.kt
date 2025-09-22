package ai.digital.integration.server.common.util

import org.apache.commons.io.output.NullOutputStream
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

class ProcessUtil {
    companion object {

        private fun createRunCommand(baseCommand: String, runLocalShell: Boolean): MutableList<String> {
            return if (runLocalShell) {
                if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                    mutableListOf("cmd", "/c", "${baseCommand}.cmd")
                } else {
                    mutableListOf("./${baseCommand}.sh")
                }
            } else {
                mutableListOf(baseCommand)
            }
        }

        fun execAndCheck(config: Map<String, Any>, logFile: File) {
            if (exec(config).exitValue() == 1) {
                throw RuntimeException("Running process was not successfully executed. Check logs [$logFile] for more information.")
            }
        }

        fun execute(project: Project, exec: String, arguments: List<String>, logOutput: Boolean = true): String {
            project.logger.lifecycle("About to execute `$exec ${arguments.joinToString(" ")}`")

            // Use ProcessBuilder instead of deprecated execOperations
            val command = mutableListOf(exec).apply { addAll(arguments) }
            val processBuilder = ProcessBuilder(command)

            if (logOutput) {
                try {
                    val process = processBuilder.start()
                    val output = process.inputStream.bufferedReader().use { it.readText() }
                    val exitCode = process.waitFor()

                    if (exitCode != 0) {
                        val error = process.errorStream.bufferedReader().use { it.readText() }
                        project.logger.error("Command failed with exit code $exitCode: $error")
                    } else {
                        project.logger.lifecycle(output)
                    }
                    return output
                } catch (e: Exception) {
                    project.logger.error("Failed to execute command: $exec ${arguments.joinToString(" ")}", e)
                    throw e
                }
            } else {
                try {
                    val process = processBuilder
                        .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                        .redirectError(ProcessBuilder.Redirect.DISCARD)
                        .start()
                    process.waitFor()
                } catch (e: Exception) {
                    project.logger.error("Failed to execute command: $exec ${arguments.joinToString(" ")}", e)
                    throw e
                }
            }
            return ""
        }

        @Suppress("UNCHECKED_CAST")
        fun exec(config: Map<String, Any?>): Process {

            val runLocalShell = config.getOrDefault("runLocalShell", true) as Boolean

            val command = createRunCommand(config["command"] as String, runLocalShell)

            if (config["params"] != null) {
                command.addAll(config["params"] as List<String>)
            }

            val processBuilder = ProcessBuilder(command)
            if (config["environment"] != null) {
                processBuilder.environment().putAll(config["environment"] as Map<String, String>)
            }

            if (config["workDir"] != null) {
                processBuilder.directory(config["workDir"] as File)
            }

            if (config["inheritIO"] != null) {
                processBuilder.inheritIO()
            }

            if (config["discardIO"] != null) {
                processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD)
                processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD)
            }

            if (config["redirectTo"] != null) {
                processBuilder.redirectErrorStream(true)
                val redirectTo = config["redirectTo"] as File
                if (!redirectTo.parentFile.isDirectory) {
                    redirectTo.parentFile.mkdirs()
                }
                processBuilder.redirectOutput(ProcessBuilder.Redirect.to(redirectTo))
            }

            val process = processBuilder.start()
            if (config["wait"] != null) {
                process.waitFor()
            }

            return process
        }

        fun chMod(project: Project, mode: String, fileName: String) {
            // Use ProcessBuilder instead of deprecated execOperations
            val command = listOf("chmod", "-R", mode, fileName)
            val processBuilder = ProcessBuilder(command)

            try {
                val process = processBuilder.start()
                val exitCode = process.waitFor()
                if (exitCode != 0) {
                    project.logger.warn("chmod command failed with exit code: $exitCode")
                }
            } catch (e: Exception) {
                project.logger.error("Failed to execute chmod command", e)
            }
        }

        fun executeCommand(
            command: String,
            workDir: File? = null,
            logOutput: Boolean = true,
            throwErrorOnFailure: Boolean = true,
            waitTimeoutSeconds: Long = 10
        ): String {
            return executeCommand(null, command, workDir, logOutput, throwErrorOnFailure, waitTimeoutSeconds)
        }

        fun executeCommand(
            project: Project?, command: String,
            workDir: File? = null,
            logOutput: Boolean = true,
            throwErrorOnFailure: Boolean = true,
            waitTimeoutSeconds: Long = 10
        ): String {
            fun print(msg: String, error: Boolean = false) {
                if (project != null && msg.isNotEmpty() && logOutput) {
                    if (error) {
                        project.logger.error(msg)
                    } else {
                        project.logger.lifecycle(msg)
                    }
                }
            }

            val execCommand = arrayOf("sh", "-c", command)
            val process: Process =
                if (workDir != null)
                    Runtime.getRuntime().exec(execCommand, null, workDir)
                else
                    Runtime.getRuntime().exec(execCommand)

            val stdInput = BufferedReader(InputStreamReader(process.inputStream))
            val stdError = BufferedReader(InputStreamReader(process.errorStream))

            if (workDir == null) {
                print("About to execute `$command`")
            } else {
                print("About to execute `$command` in work dir `${workDir.absolutePath}`")
            }

            val input = readLines(stdInput) { line -> print(line) }
            val error = readLines(stdError) { line -> print(line, true) }

            if (process.waitFor(waitTimeoutSeconds, TimeUnit.SECONDS)) {
                if (throwErrorOnFailure && process.exitValue() != 0) {
                    throw RuntimeException("Process '$command' failed with exit value ${process.exitValue()}: $error")
                }
            } else if (throwErrorOnFailure) {
                throw RuntimeException("Process '$command' not finished")
            }

            return if (error.isEmpty()) {
                input
            } else {
                input + System.lineSeparator() + error
            }
        }

        private fun readLines(reader: BufferedReader, lineHandler: (String) -> Unit): String {
            var result = ""
            var line = ""
            while (reader.readLine().also { if (it != null) line = it } != null) {
                line.also {
                    if (result.isNotEmpty())
                        result += System.lineSeparator()
                    result += it
                }
                lineHandler(line)
            }
            return result
        }
    }
}
