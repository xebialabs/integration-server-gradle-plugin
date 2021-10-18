package ai.digital.integration.server.common.util

import org.gradle.api.Project
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
    }
}
