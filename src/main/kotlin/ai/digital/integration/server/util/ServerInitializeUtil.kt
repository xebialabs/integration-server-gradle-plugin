package ai.digital.integration.server.util

import ai.digital.integration.server.domain.Server
import org.gradle.api.Project
import java.io.File

class ServerInitializeUtil {
    companion object {
        @JvmStatic
        private fun createFolders(project: Project) {
            project.logger.lifecycle("Preparing server destination folders.")

            arrayOf("centralConfiguration", "conf", "hotfix/plugins", "hotfix/lib", "plugins").forEach { folderName ->
                val folderPath = "${DeployServerUtil.getServerWorkingDir(project)}/${folderName}"
                val folder = File(folderPath)
                folder.mkdirs()
                project.logger.lifecycle("Folder $folderPath has created.")
            }
        }

        @JvmStatic
        private fun createConfFile(project: Project, server: Server) {
            project.logger.lifecycle("Creating deployit.conf file")

            val file = project.file("${DeployServerUtil.getServerWorkingDir(project)}/conf/deployit.conf")
            file.createNewFile()

            file.writeText("http.port=${server.httpPort}\n")
            file.appendText("http.bind.address=0.0.0.0\n")
            file.appendText("http.context.root=${server.contextRoot}\n")
            file.appendText("threads.min=3\n")
            file.appendText("threads.max=24\n")
            file.appendText("xl.spring.cloud.enabled=true\n")

        }

        @JvmStatic
        fun prepare(project: Project) {
            val server = DeployServerUtil.getServer(project)
            project.logger.lifecycle("Preparing serve ${server.name} before launching it.")
            createFolders(project)
            createConfFile(project, server)
        }
    }
}
