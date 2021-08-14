package ai.digital.integration.server.util

import ai.digital.integration.server.domain.Server
import org.gradle.api.Project

import java.nio.file.Paths

class CliUtil {

    static def getWorkingDir(Project project) {
        Server server = ServerUtil.getServer(project)
        def targetDir = ServerUtil.getServerWorkingDir(project).toString()
        Paths.get(targetDir, "xl-deploy-${server.version}-cli").toAbsolutePath().toString()
    }

    static def getCliBin(Project project) {
        Paths.get(getWorkingDir(project), "bin").toFile()
    }
}
