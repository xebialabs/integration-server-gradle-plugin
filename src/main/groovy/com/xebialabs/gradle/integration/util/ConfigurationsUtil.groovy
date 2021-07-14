package com.xebialabs.gradle.integration.util

import org.gradle.api.Project

class ConfigurationsUtil {
    static def SERVER_DIST_CONFIG = "integrationServerDist"
    static def SERVER_CLI_DIST_CONFIG = "integrationServerCli"
    static def SERVER_DATA_DIST = "integrationServerData"
    static def SATELLITE_DATA_DIST = "integrationSatelliteDist"

    static void registerConfigurations(Project project) {
        project.buildscript.configurations.create(SERVER_DIST_CONFIG)
        project.buildscript.configurations.create(SERVER_CLI_DIST_CONFIG)
        project.buildscript.configurations.create(SERVER_DATA_DIST)
        project.buildscript.configurations.create(SATELLITE_DATA_DIST)
    }
}
