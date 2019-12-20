package com.xebialabs.gradle.integration.util

import org.gradle.api.Project

class ConfigurationsUtil {
    static def SERVER_DIST_CONFIG = "integrationServerDist"
    static def SERVER_CLI_DIST_CONFIG = "integrationServerCli"
    static def SERVER_LIB_CONFIG = "integrationServerLib"
    static def SERVER_PLUGIN_CONFIG = "integrationServerPlugin"
    static def SERVER_PROVISION_SCRIPT_CONFIG = "integrationServerProvisionScript"

    static void registerConfigurations(Project project) {
        project.buildscript.configurations.create(SERVER_DIST_CONFIG)
        project.buildscript.configurations.create(SERVER_CLI_DIST_CONFIG)
        project.buildscript.configurations.create(SERVER_LIB_CONFIG)
        project.buildscript.configurations.create(SERVER_PLUGIN_CONFIG)
        project.buildscript.configurations.create(SERVER_PROVISION_SCRIPT_CONFIG)
    }
}
