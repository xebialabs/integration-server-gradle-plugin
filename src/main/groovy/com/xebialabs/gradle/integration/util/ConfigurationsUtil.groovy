package com.xebialabs.gradle.integration.util

import org.gradle.api.Project

class ConfigurationsUtil {
    static def SERVER_DIST = "integrationServerDist"
    static def SERVER_CLI_DIST = "integrationServerCli"
    static def SERVER_DATA_DIST = "integrationServerData"
    static def SATELLITE_DATA_DIST = "integrationSatelliteDist"
    static def CENTRAL_CONFIG_DATA_DIST = "integrationCentralConfigDist"
    static def INTEGRATION_TEST_SERVER = "integrationTestServer"
    static def INTEGRATION_TEST_CLI = "integrationTestCli"

    static void registerConfigurations(Project project) {
        project.buildscript.configurations.create(SERVER_DIST)
        project.buildscript.configurations.create(SERVER_CLI_DIST)
        project.buildscript.configurations.create(SERVER_DATA_DIST)
        project.buildscript.configurations.create(SATELLITE_DATA_DIST)
        project.buildscript.configurations.create(CENTRAL_CONFIG_DATA_DIST)
    }
}

