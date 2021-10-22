package ai.digital.integration.server.deploy.internals

import ai.digital.integration.server.common.domain.Server
import org.gradle.api.Project
import java.io.File

class DeployServerInitializeUtil {
    companion object {
        private fun createFolders(project: Project, server: Server) {
            project.logger.lifecycle("Preparing server destination folders.")

            arrayOf("centralConfiguration", "conf", "hotfix/plugins", "hotfix/lib", "plugins").forEach { folderName ->
                val folderPath = "${DeployServerUtil.getServerWorkingDir(project, server)}/${folderName}"
                val folder = File(folderPath)
                folder.mkdirs()
                project.logger.lifecycle("Folder $folderPath has been created.")
            }
        }

        private fun createConfFile(project: Project, server: Server) {
            project.logger.lifecycle("Creating deployit.conf file for ${server.name}")

            val file = project.file("${DeployServerUtil.getServerWorkingDir(project, server)}/conf/deployit.conf")
            file.createNewFile()

            file.writeText("http.port=${server.httpPort}\n")
            file.appendText("http.bind.address=0.0.0.0\n")
            file.appendText("http.context.root=${server.contextRoot}\n")
            file.appendText("threads.min=3\n")
            file.appendText("threads.max=24\n")
            file.appendText("xl.spring.cloud.enabled=true\n")
        }

        fun prepare(project: Project, server: Server) {
            project.logger.lifecycle("Preparing server ${server.name} before launching it.")
            createFolders(project, server)
            createConfFile(project, server)
        }
    }
}
