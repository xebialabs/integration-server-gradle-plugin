package ai.digital.integration.server.deploy.internals

import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.util.CentralConfigurationServerUtil
import org.gradle.api.Project
import java.io.File

class DeployServerInitializeUtil {
    companion object {
        private fun createFolders(project: Project, server: Server) {
            project.logger.lifecycle("Preparing server destination folders.")

            arrayOf("centralConfiguration", "hotfix/plugins", "hotfix/lib", "plugins").forEach { folderName ->
                val folderPath = "${DeployServerUtil.getServerWorkingDir(project, server)}/${folderName}"
                val folder = File(folderPath)
                folder.mkdirs()
                project.logger.lifecycle("Folder $folderPath has been created.")
            }
        }

        private fun createConfFile(project: Project, server: Server, auxiliaryServer: Boolean = false) {
            project.logger.lifecycle("Creating deployit.conf file for ${server.name}")

            val file = project.file("${DeployServerUtil.getServerWorkingDir(project, server)}/conf/deployit.conf")
            if (!file.parentFile.exists()) {
                file.parentFile.mkdirs()
            }
            file.createNewFile()

            if (!auxiliaryServer) {
                file.writeText("http.port=${server.httpPort}\n")
                file.appendText("http.context.root=${server.contextRoot}\n")
            } else {
                file.writeText("http.port=4516\n")
                file.appendText("http.context.root=/\n")
            }

            file.appendText("http.bind.address=0.0.0.0\n")
            file.appendText("server.hostname=127.0.0.1\n")
            file.appendText("threads.min=3\n")
            file.appendText("threads.max=24\n")
            file.appendText("server.hostname=127.0.0.1\n")
            file.appendText("server.port=8180\n")
            file.appendText("xl.spring.cloud.enabled=true\n")

            if(CentralConfigurationServerUtil.hasCentralConfigurationServer(project)) {
                val cc = CentralConfigurationServerUtil.getCentralConfigurationServer(project)
                file.appendText("xl.spring.cloud.uri=http://localhost:${cc.httpPort}/centralConfiguration/\n")
                file.appendText("xl.spring.cloud.external-config=true\n")
            }
        }

        fun prepare(project: Project, server: Server, auxiliaryServer: Boolean = false) {
            project.logger.lifecycle("Preparing Deploy server ${server.name} before launching it.")
            createFolders(project, server)
            createConfFile(project, server, auxiliaryServer)
        }
    }
}
