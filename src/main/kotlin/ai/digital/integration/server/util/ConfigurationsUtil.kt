package ai.digital.integration.server.util

import org.gradle.api.Project

class ConfigurationsUtil {
    companion object {
        @JvmStatic
        val SERVER_DIST = "serverDist"

        @JvmStatic
        val SERVER_CLI_DIST = "serverCliDist"

        @JvmStatic
        val SERVER_DATA_DIST = "serverDataDist"

        @JvmStatic
        val SATELLITE_DIST = "satelliteDist"

        @JvmStatic
        val CENTRAL_CONFIG_DIST = "centralConfigDist"

        @JvmStatic
        val WORKER_DIST = "workerDist"

        @JvmStatic
        val DEPLOY_SERVER = "integrationTestServer" // TODO: review and refactor

        @JvmStatic
        fun registerConfigurations(project: Project) {
            project.buildscript.configurations.create(SERVER_DIST)
            project.buildscript.configurations.create(SERVER_CLI_DIST)
            project.buildscript.configurations.create(SERVER_DATA_DIST)
            project.buildscript.configurations.create(SATELLITE_DIST)
            project.buildscript.configurations.create(CENTRAL_CONFIG_DIST)
            project.buildscript.configurations.create(WORKER_DIST)
        }
    }
}
