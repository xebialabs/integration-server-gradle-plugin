package com.xebialabs.gradle.integration.util

import org.gradle.api.Project

import java.nio.file.Paths

import static com.xebialabs.gradle.integration.util.PluginUtil.DIST_DESTINATION_NAME

class DockerComposeUtil {

    static def dockerComposeFileName(Project project) {
        def dbName = DbUtil.databaseName(project)
        return "docker-compose_${dbName}.yaml"
    }

    static def dockerComposeFileDestination(Project project) {
        def composeFile = dockerComposeFileName(project)
        return Paths.get(
            "${project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString()}/${composeFile}")
    }

    static def dockerfileDestination(Project project, String filename) {
        return Paths.get(
                "${project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString()}/${filename}")

    }

}
