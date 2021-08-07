package com.xebialabs.gradle.integration.util


import com.xebialabs.gradle.integration.domain.Server
import org.gradle.api.Project

import java.nio.file.Path
import java.nio.file.Paths

import static com.xebialabs.gradle.integration.constant.PluginConstant.DIST_DESTINATION_NAME

class LocationUtil {

    static Path getServerDir(Project project) {
        return Paths.get(project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString())
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

    static def getSatelliteWorkingDir(Project project) {
        def satellite = ExtensionUtil.getSatellite(project)
        def targetDir = project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString()
        Paths.get(targetDir, "xl-satellite-server-${satellite.version}").toAbsolutePath().toString()
    }
}
