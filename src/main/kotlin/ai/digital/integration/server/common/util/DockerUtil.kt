package ai.digital.integration.server.common.util

import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

class DockerUtil {
    companion object {
        fun execute(project: Project, args: List<String>, logOutput: Boolean = true): String {
            return ProcessUtil.execute(project, "docker", args, logOutput)
        }

        fun inspect(project: Project, format: String, instanceId: String): String {
            val stdout = ByteArrayOutputStream()
            project.exec {
                executable = "docker"
                args = listOf(
                    "inspect",
                    "-f",
                    format,
                    instanceId
                )
                standardOutput = stdout
            }

            return stdout.toString(StandardCharsets.UTF_8).trim()
        }

        private fun findContainerIdByName(project: Project, containerName: String): String {
            val args = arrayListOf("ps", "-a", "-f", "name=$containerName", "--format", "{{.ID}}")
            return execute(project, args, true).trim()
        }

        fun dockerLogs(project: Project, containerName: String): String {
            val containerId = findContainerIdByName(project, containerName)
            val args = arrayListOf("logs", containerId)
            return execute(project, args, true)
        }
    }
}
