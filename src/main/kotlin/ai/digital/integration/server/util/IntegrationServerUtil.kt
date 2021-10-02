package ai.digital.integration.server.util

import ai.digital.integration.server.constant.PluginConstant.DIST_DESTINATION_NAME
import org.gradle.api.Project

class IntegrationServerUtil {

    companion object {
        @JvmStatic
        fun getDist(project: Project): String {
            return project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString()
        }
    }
}
