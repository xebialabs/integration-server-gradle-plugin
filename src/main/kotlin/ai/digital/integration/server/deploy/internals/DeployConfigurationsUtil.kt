package ai.digital.integration.server.deploy.internals

import org.gradle.api.Project

class DeployConfigurationsUtil {
    companion object {
        const val SERVER_DIST = "serverDist"
        const val SERVER_CLI_DIST = "serverCliDist"
        const val SERVER_DATA_DIST = "serverDataDist"
        const val SATELLITE_DIST = "satelliteDist"
        const val CENTRAL_CONFIG_DIST = "centralConfigDist"
        const val WORKER_DIST = "workerDist"
        const val DEPLOY_SERVER = "integrationTestServer" // TODO: review and refactor
        const val OPERATOR_DIST = "operatorDist"
        const val XL_CLI_DIST = "xlCliDist"

        fun registerConfigurations(project: Project) {
            project.buildscript.configurations.create(SERVER_DIST)
            project.buildscript.configurations.create(SERVER_CLI_DIST)
            project.buildscript.configurations.create(SERVER_DATA_DIST)
            project.buildscript.configurations.create(SATELLITE_DIST)
            project.buildscript.configurations.create(CENTRAL_CONFIG_DIST)
            project.buildscript.configurations.create(WORKER_DIST)
            project.buildscript.configurations.create(OPERATOR_DIST)
            project.buildscript.configurations.create(XL_CLI_DIST)
        }
    }
}
