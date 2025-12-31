package ai.digital.integration.server.common.util

import org.gradle.api.Project
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class DockerUtil {
    companion object {

        fun execute(project: Project, args: List<String>, logOutput: Boolean = true, throwErrorOnFailure: Boolean = true): String {
            return ProcessUtil.executeCommand(project, "docker ${args.joinToString(" ")}",
                    logOutput = logOutput, throwErrorOnFailure = throwErrorOnFailure)
        }

        fun inspect(project: Project, format: String, instanceId: String): String {
            val stdout = ByteArrayOutputStream()
            val execOps = project.serviceOf<ExecOperations>()
            execOps.exec {
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
            return execute(project, args, false).trim()
        }

        fun dockerLogs(project: Project, containerName: String, lastUpdate: LocalDateTime): String {
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            val containerId = findContainerIdByName(project, containerName)
            val args = arrayListOf("logs", containerId, "--since", lastUpdate.format(formatter))
            return execute(project, args, false)
        }
    }
}
