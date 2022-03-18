package ai.digital.integration.server.release.internals

import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.release.util.ReleaseServerUtil
import org.gradle.api.Project
import java.io.File

class ReleaseServerInitializeUtil {
    companion object {
        private fun createFolders(project: Project) {
            project.logger.lifecycle("Preparing server destination folders.")

            arrayOf("centralConfiguration", "hotfix/plugins", "hotfix/lib", "plugins", "conf").forEach { folderName ->
                val folderPath = "${ReleaseServerUtil.getServerWorkingDir(project)}/${folderName}"
                val folder = File(folderPath)
                folder.mkdirs()
                project.logger.lifecycle("Folder $folderPath has been created.")
            }
        }

        fun prepare(project: Project, server: Server) {
            project.logger.lifecycle("Preparing Release server ${server.name} before launching it.")
            createFolders(project)
        }
    }
}
