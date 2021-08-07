package com.xebialabs.gradle.integration.util

import com.xebialabs.gradle.integration.IntegrationServerExtension
import com.xebialabs.gradle.integration.domain.Server
import org.gradle.api.Project

class ServerUtil {

    static Server getServer(Project project) {
        def ext = project.extensions.getByType(IntegrationServerExtension)
        def server = ext.servers.first()
        server.setVersion(getServerVersion(project, server))
        server.setHttpPort(getHttpPort(project, server))
        server
    }

    private static String getServerVersion(Project project, Server server) {
        project.hasProperty("xlDeployVersion") ? project.property("xlDeployVersion") : server.version
    }

    private static Integer getHttpPort(Project project, Server server) {
        project.hasProperty("serverHttpPort") ? Integer.valueOf(project.property("serverHttpPort").toString()) : server.httpPort
    }
}
