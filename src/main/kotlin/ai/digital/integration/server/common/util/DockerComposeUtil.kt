package ai.digital.integration.server.common.util

import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Path

class DockerComposeUtil {
    companion object {
        fun getResolvedDockerPath(project: Project, relativePath: String): Path {
            val dockerComposeStream = {}::class.java.classLoader.getResourceAsStream(relativePath)
            val resultComposeFilePath = ServerUtil.getRelativePathInIntegrationServerDist(project, relativePath)
            dockerComposeStream?.let {
                FileUtil.copyFile(it, resultComposeFilePath)
            }
            return resultComposeFilePath
        }

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
