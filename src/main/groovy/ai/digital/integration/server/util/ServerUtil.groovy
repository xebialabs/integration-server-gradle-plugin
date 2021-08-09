package ai.digital.integration.server.util

import ai.digital.integration.server.IntegrationServerExtension
import ai.digital.integration.server.domain.Server
import org.gradle.api.Project

class ServerUtil {

    static Server getServer(Project project) {
        def ext = project.extensions.getByType(IntegrationServerExtension)
        def server = ext.servers.first()
        server.setDebugPort(getDebugPort(project, server))
        server.setHttpPort(getHttpPort(project, server))
        server.setVersion(getServerVersion(project, server))
        server
    }

    private static String getServerVersion(Project project, Server server) {
        project.hasProperty("xlDeployVersion") ? project.property("xlDeployVersion") : server.version
    }

    private static Integer getHttpPort(Project project, Server server) {
        project.hasProperty("serverHttpPort") ? Integer.valueOf(project.property("serverHttpPort").toString()) : server.httpPort
    }

    private static Integer getDebugPort(Project project, Server server) {
        project.hasProperty("serverDebugPort") ? Integer.valueOf(project.property("serverDebugPort").toString()) : server.debugPort
    }
}
