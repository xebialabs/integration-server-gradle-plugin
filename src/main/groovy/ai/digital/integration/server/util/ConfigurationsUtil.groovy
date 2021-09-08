package ai.digital.integration.server.util

import org.gradle.api.Project

class ConfigurationsUtil {
    static def SERVER_DIST = "serverDist"
    static def SERVER_CLI_DIST = "serverCliDist"
    static def SERVER_DATA_DIST = "serverDataDist"
    static def SATELLITE_DIST = "satelliteDist"
    static def CENTRAL_CONFIG_DIST = "centralConfigDist"
    static def WORKER_DIST = "workerDist"

    static def DEPLOY_SERVER = "integrationTestServer" // TODO: review and refactor

    static void registerConfigurations(Project project) {
        project.buildscript.configurations.create(SERVER_DIST)
        project.buildscript.configurations.create(SERVER_CLI_DIST)
        project.buildscript.configurations.create(SERVER_DATA_DIST)
        project.buildscript.configurations.create(SATELLITE_DIST)
        project.buildscript.configurations.create(CENTRAL_CONFIG_DIST)
        project.buildscript.configurations.create(WORKER_DIST)
    }
}

