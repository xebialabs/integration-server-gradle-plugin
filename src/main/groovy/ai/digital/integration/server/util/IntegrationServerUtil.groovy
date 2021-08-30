package ai.digital.integration.server.util

import org.gradle.api.Project

import static ai.digital.integration.server.constant.PluginConstant.DIST_DESTINATION_NAME

class IntegrationServerUtil {

    static String getDist(Project project) {
        project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString()
    }
}
