package com.xebialabs.gradle.integration.util

import org.gradle.api.Project

import java.nio.file.Path
import java.nio.file.Paths

import static com.xebialabs.gradle.integration.constant.PluginConstant.DIST_DESTINATION_NAME

class DockerComposeUtil {

    static def dockerComposeFileDestination(Project project, String composeFileName) {
        return Paths.get(
                "${project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString()}/${composeFileName}")
    }

    static def dockerfileDestination(Project project, String filename) {
        return Paths.get(
                "${project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString()}/${filename}")

    }

    static def getResolvedDockerPath(Project project, String relativePath) {
        project.logger.lifecycle("Resolving docker path $relativePath")
        def dockerComposeStream = DockerComposeUtil.class.classLoader.getResourceAsStream(relativePath)
        Path resultComposeFilePath = dockerComposeFileDestination(project, relativePath)
        FileUtil.copyFile(dockerComposeStream, resultComposeFilePath)
        return resultComposeFilePath
    }

}
