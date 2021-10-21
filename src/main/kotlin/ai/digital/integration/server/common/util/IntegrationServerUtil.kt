package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.constant.PluginConstant.DIST_DESTINATION_NAME
import org.gradle.api.Project
import java.nio.file.Path
import java.nio.file.Paths

class IntegrationServerUtil {

    companion object {
        fun getDist(project: Project): String {
            return project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString()
        }

        fun getRelativePathInIntegrationServerDist(project: Project, relativePath: String): Path {
            return Paths.get("${getDist(project)}/${relativePath}")
        }
    }
}
