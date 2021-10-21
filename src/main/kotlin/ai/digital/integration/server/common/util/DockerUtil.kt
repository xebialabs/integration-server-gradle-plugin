package ai.digital.integration.server.common.util

import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

class DockerUtil {
    companion object {
        fun inspect(project: Project, format: String, instanceId: String): String {
            val stdout = ByteArrayOutputStream()
            project.exec {
                it.executable = "docker"
                it.args = listOf(
                    "inspect",
                    "-f",
                    format,
                    instanceId
                )
                it.standardOutput = stdout
            }

            return stdout.toString(StandardCharsets.UTF_8).trim()
        }
    }
}
