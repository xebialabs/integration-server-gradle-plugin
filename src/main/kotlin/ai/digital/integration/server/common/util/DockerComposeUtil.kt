package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.Server
import org.apache.tools.ant.taskdefs.condition.Os
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
            // On Windows, use 'docker compose' instead of 'docker-compose'
            val executable = if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                "docker"
            } else {
                "docker-compose"
            }
            
            // If using docker on Windows, prepend 'compose' to arguments
            val fullArgs = if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                listOf("compose") + args
            } else {
                args
            }
            
            return ProcessUtil.execute(project, executable, fullArgs, logOutput)
        }

        fun allowToCleanMountedFiles(
                project: Project,
                productName: ProductName,
                server: Server,
                dockerComposeFile: File
        ) {
            val name = productName.toString().lowercase()
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
