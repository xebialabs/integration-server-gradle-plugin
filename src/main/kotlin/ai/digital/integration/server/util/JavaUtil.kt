package ai.digital.integration.server.util

import org.gradle.api.Project
import org.jetbrains.kotlin.konan.file.File

class JavaUtil {
    companion object {

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun execJava(config: Map<String, Any>): Process {
            val jvmPath = config.getOrDefault("jvmPath", "${File.javaHome}${File.separator}bin${File.separator}java") as String
            val jvmArgs = config.getOrDefault("jvmArgs", listOf<String>()) as List<String>
            val programArgs = config.getOrDefault("programArgs", listOf<String>()) as List<String>
            val mainClass = config["mainClass"] as String
            val classpath = config["classpath"] as String
            val configEnvironment = config.getOrDefault("environment", mapOf<String, String>()) as Map<String, String>

            val environment = mutableMapOf<String, String>()
            environment.putAll(configEnvironment)
            environment["CLASSPATH"] = classpath

            val params = mutableListOf<String>()
            params.addAll(jvmArgs)
            params.add(mainClass)
            params.addAll(programArgs)

            val command = mutableMapOf<String, Any>()
            command.putAll(config)
            command["command"] = jvmPath
            command["runLocalShell"] = false
            command["params"] = params
            command["environment"] = environment

            return ProcessUtil.exec(command)
        }

        @JvmStatic
        fun debugJvmArg(project: Project, debugPort: Int, debugSuspend: Boolean): List<String> {
            project.logger.lifecycle("Enabled debug mode on port $debugPort")
            return listOf(
                    "-Xdebug",
                    DeployServerUtil.createDebugString(debugSuspend, debugPort)
            )
        }

        @JvmStatic
        fun jvmPath(project: Project, integrationServerJVMPath: String): Map<String, String> {
            val jvmPath = "$integrationServerJVMPath${File.separator}bin${File.separator}java"
            project.logger.lifecycle("Using JVM from location: $jvmPath")
            return mapOf("jvmPath" to jvmPath)
        }
    }
}
