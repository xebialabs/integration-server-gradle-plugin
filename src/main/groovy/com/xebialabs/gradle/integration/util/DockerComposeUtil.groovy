package com.xebialabs.gradle.integration.util

import org.gradle.api.Project

import java.nio.file.Paths

import static com.xebialabs.gradle.integration.util.PluginUtil.DIST_DESTINATION_NAME

class DockerComposeUtil {

    static def dockerComposeFileName(project) {
        def dbName = DbUtil.databaseName(project)
        return "docker-compose_${dbName}.yaml"
    }

    static def dockerComposeFileDestination(Project project) {
        def composeFile = dockerComposeFileName(project)
        return dockerComposeFileDestination(project, composeFile)
    }

    static def dockerComposeFileDestination(Project project, String fileName) {
        return Paths.get(
                "${project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString()}/${fileName}"
        )
    }

}
