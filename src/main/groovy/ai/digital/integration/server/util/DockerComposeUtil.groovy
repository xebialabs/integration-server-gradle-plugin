package ai.digital.integration.server.util

import org.gradle.api.Project

import java.nio.file.Path
import java.nio.file.Paths

class DockerComposeUtil {

    static def dockerComposeFileDestination(Project project, String composeFileName) {
        Paths.get("${ServerUtil.getServerDistFolder(project)}/${composeFileName}")
    }

    static def dockerfileDestination(Project project, String filename) {
        Paths.get("${ServerUtil.getServerDistFolder(project)}/${filename}")
    }

    static Path getResolvedDockerPath(Project project, String relativePath) {
        def dockerComposeStream = DockerComposeUtil.class.classLoader.getResourceAsStream(relativePath)
        Path resultComposeFilePath = dockerComposeFileDestination(project, relativePath)
        FileUtil.copyFile(dockerComposeStream, resultComposeFilePath)
        resultComposeFilePath
    }

}
