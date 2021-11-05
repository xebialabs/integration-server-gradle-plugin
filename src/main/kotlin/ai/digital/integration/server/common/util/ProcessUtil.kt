package ai.digital.integration.server.common.util

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets

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

        fun execute(project: Project, executable: String, args: List<String>, logOutput: Boolean = true): String {
            project.logger.lifecycle("About to execute `$executable ${args.joinToString(" ")}`")
            val stdout = ByteArrayOutputStream()
            project.exec {
                it.args = args
                it.executable = executable
                it.standardOutput = stdout
            }
            val output = stdout.toString(StandardCharsets.UTF_8)
            if (logOutput) {
                project.logger.lifecycle(output)
            }
            return output
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
            project.exec {
                it.executable = "chmod"
                it.args = listOf("-R", mode, fileName)
            }
        }
    }
}
