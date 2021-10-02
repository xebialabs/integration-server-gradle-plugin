package ai.digital.integration.server.util

import ai.digital.integration.server.domain.Server
import org.gradle.api.Project

class ServerInitializeUtil {

    private static void createFolders(Project project) {
        project.logger.lifecycle("Preparing server destination folders.")

        ["centralConfiguration", "conf", "hotfix/plugins", "hotfix/lib", "plugins"].each { String folderName ->
            def folderPath = "${DeployServerUtil.getServerWorkingDir(project)}/${folderName}"
            def folder = new File(folderPath)
            folder.mkdirs()
            project.logger.lifecycle("Folder $folderPath has created.")
        }
    }

    private static void createConfFile(Project project, Server server) {
        project.logger.lifecycle("Creating deployit.conf file")

        def file = project.file("${DeployServerUtil.getServerWorkingDir(project)}/conf/deployit.conf")
        file.createNewFile()
        file.withWriter { BufferedWriter w ->
            w.write("http.port=${server.httpPort}\n")
            w.write("http.bind.address=0.0.0.0\n")
            w.write("http.context.root=${server.contextRoot}\n")
            w.write("threads.min=3\n")
            w.write("threads.max=24\n")
        }
    }

    static def prepare(Project project) {
        Server server = DeployServerUtil.getServer(project)
        project.logger.lifecycle("Preparing serve ${server.name} before launching it.")
        createFolders(project)
        createConfFile(project, server)
    }
}
