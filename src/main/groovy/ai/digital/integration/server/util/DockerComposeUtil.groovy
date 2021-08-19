package ai.digital.integration.server.util

import org.gradle.api.Project

import java.nio.file.Path

class DockerComposeUtil {

    static Path getResolvedDockerPath(Project project, String relativePath) {
        def dockerComposeStream = DockerComposeUtil.class.classLoader.getResourceAsStream(relativePath)
        Path resultComposeFilePath = ServerUtil.getRelativePathInServerDist(project, relativePath)
        FileUtil.copyFile(dockerComposeStream, resultComposeFilePath)
        resultComposeFilePath
    }

}
