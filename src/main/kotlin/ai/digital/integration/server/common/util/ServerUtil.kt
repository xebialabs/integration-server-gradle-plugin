package ai.digital.integration.server.common.util

import org.gradle.api.Project
import java.nio.file.Path
import java.nio.file.Paths

class ServerUtil {
    companion object {
        fun getRelativePathInIntegrationServerDist(project: Project, relativePath: String): Path {
            return Paths.get("${IntegrationServerUtil.getDist(project)}/${relativePath}")
        }
    }
}
