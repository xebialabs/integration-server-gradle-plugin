package ai.digital.integration.server.common.util

import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Path

class DockerComposeUtil {
    companion object {
        fun getResolvedDockerPath(project: Project, relativePath: String): Path {
            val dockerComposeStream = {}::class.java.classLoader.getResourceAsStream(relativePath)
            val resultComposeFilePath =
                IntegrationServerUtil.getRelativePathInIntegrationServerDist(project, relativePath)
            dockerComposeStream?.let {
                FileUtil.copyFile(it, resultComposeFilePath)
            }
            return resultComposeFilePath
        }

        fun execute(project: Project, args: List<String>, logOutput: Boolean = true): String {
            val stdout = ByteArrayOutputStream()
            project.exec {
                it.args = args
                it.executable = "docker-compose"
                it.standardOutput = stdout
            }
            val output = stdout.toString(StandardCharsets.UTF_8)
            if (logOutput) {
                project.logger.lifecycle(output)
            }
            return output
        }
    }
}
