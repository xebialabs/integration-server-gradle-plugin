package com.xebialabs.gradle.integration.util

import org.gradle.api.Project

class ConfigurationsUtil {
    static def SERVER_DIST = "serverDist"
    static def SERVER_CLI_DIST = "serverCli"
    static def SERVER_DATA_DIST = "integrationServerData"
    static def SATELLITE_DIST = "satellite"
    static def CENTRAL_CONFIG_DIST = "centralConfigDist"

    static def DEPLOY_SERVER = "integrationTestServer" // TODO: review and refactor
    static def DEPLOY_CLI = "integrationTestCli" // TODO: review and refactor

    static void registerConfigurations(Project project) {
        project.buildscript.configurations.create(SERVER_DIST)
        project.buildscript.configurations.create(SERVER_CLI_DIST)
        project.buildscript.configurations.create(SERVER_DATA_DIST)
        project.buildscript.configurations.create(SATELLITE_DIST)
        project.buildscript.configurations.create(CENTRAL_CONFIG_DIST)
    }
}

