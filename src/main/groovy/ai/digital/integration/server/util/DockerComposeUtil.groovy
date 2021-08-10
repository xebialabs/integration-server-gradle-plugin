package ai.digital.integration.server.util

import org.gradle.api.Project

import java.nio.file.Path
import java.nio.file.Paths

import static ai.digital.integration.server.constant.PluginConstant.DIST_DESTINATION_NAME

class DockerComposeUtil {

    static def dockerComposeFileDestination(Project project, String composeFileName) {
        Paths.get(
                "${project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString()}/${composeFileName}")
    }

    static def dockerfileDestination(Project project, String filename) {
        Paths.get(
                "${project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString()}/${filename}")
    }

    static def getResolvedDockerPath(Project project, String relativePath) {
        def dockerComposeStream = DockerComposeUtil.class.classLoader.getResourceAsStream(relativePath)
        Path resultComposeFilePath = dockerComposeFileDestination(project, relativePath)
        FileUtil.copyFile(dockerComposeStream, resultComposeFilePath)
        resultComposeFilePath
    }

}
