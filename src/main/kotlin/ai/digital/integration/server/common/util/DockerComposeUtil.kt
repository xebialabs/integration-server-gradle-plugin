package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.Server
import org.gradle.api.Project
import java.io.File
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
            return ProcessUtil.execute(project, "docker-compose", args, logOutput)
        }

        fun allowToCleanMountedFiles(
                project: Project,
                productName: ProductName,
                server: Server,
                dockerComposeFile: File
        ) {
            val name = productName.toString().toLowerCase()
            try {
                val args = arrayListOf("-f",
                        dockerComposeFile.path,
                        "exec",
                        "-T",
                        "${name}-${server.version}",
                        "chmod",
                        "777",
                        "-R",
                        "/opt/xebialabs/xl-${name}-server")
                execute(project, args, false)
            } catch (e: Exception) {
                // ignore, if throws exception, it means that docker container is not running
            }
        }
    }
}
