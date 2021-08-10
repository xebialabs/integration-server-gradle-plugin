package ai.digital.integration.server.util


import ai.digital.integration.server.domain.Server
import org.gradle.api.Project

import java.nio.file.Path
import java.nio.file.Paths

import static ai.digital.integration.server.constant.PluginConstant.DIST_DESTINATION_NAME

class LocationUtil {

    static Path getServerDir(Project project) {
        Paths.get(project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString())
    }

    static def getServerWorkingDir(Project project) {
        Server server = ServerUtil.getServer(project)

        if (server.runtimeDirectory == null) {
            def targetDir = getServerDir(project).toString()
            Paths.get(targetDir, "xl-deploy-${server.version}-server").toAbsolutePath().toString()
        } else {
            def target = project.projectDir.toString()
            Paths.get(target, server.runtimeDirectory).toAbsolutePath().toString()
        }
    }

}
