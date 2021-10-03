package ai.digital.integration.server.util

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import java.io.File

class ProcessUtil {
    companion object {
        @JvmStatic
        private fun createRunCommand(baseCommand: String): MutableList<String> {
            return if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                mutableListOf("cmd", "/c", "${baseCommand}.cmd")
            } else {
                mutableListOf("./${baseCommand}.sh")
            }
        }

        @JvmStatic
        fun execAndCheck(config: Map<String, Any>, logFile: File) {
            if (exec(config).exitValue() == 1) {
                throw RuntimeException("Running process was not successfully executed. Check logs [$logFile] for more information.")
            }
        }

        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun exec(config: Map<String, Any>): Process {
            val command = createRunCommand(config["command"] as String)

            if (config["params"] != null) {
                command.addAll(config["params"] as List<String>)
            }

            val processBuilder = ProcessBuilder(command)
            if (config["environment"] != null) {
                processBuilder.environment().putAll(config["environment"] as Map<String, String>)
            }

            processBuilder.directory(config["workDir"] as File)

            if (config["inheritIO"] != null) {
                processBuilder.inheritIO()
            }

            if (config["discardIO"] != null) {
                processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD)
                processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD)
            }

            if (config["redirectTo"] != null) {
                processBuilder.redirectErrorStream(true)
                processBuilder.redirectOutput(ProcessBuilder.Redirect.to(config["redirectTo"] as File))
            }

            val process = processBuilder.start()
            if (config["wait"] != null) {
                process.waitFor()
            }

            return process
        }

        @JvmStatic
        fun chMod(project: Project, mode: String, fileName: String) {
            project.exec {
                it.executable = "chmod"
                it.args = listOf("-R", mode, fileName)
            }
        }
    }
}
