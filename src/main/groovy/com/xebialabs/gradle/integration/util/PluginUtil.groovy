package com.xebialabs.gradle.integration.util

import org.gradle.api.Project

import java.nio.file.Paths

class PluginUtil {
    static def PLUGIN_GROUP = "Integration Server"
    static def DIST_DESTINATION_NAME = "integration-server"

    static def getDistLocation(Project project) {
        return Paths.get(
                "${project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString()}"
        )
    }
}
