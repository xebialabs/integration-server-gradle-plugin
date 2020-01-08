package com.xebialabs.gradle.integration.util

import com.xebialabs.gradle.integration.IntegrationServerExtension
import org.gradle.api.Project

import java.nio.file.Paths

class ExtensionsUtil {
    static def EXTENSION_NAME = "integrationServer"

    private static int findFreePort() {
        ServerSocket socket = null
        try {
            socket = new ServerSocket(0)
            socket.setReuseAddress(true)
            int port = socket.getLocalPort()
            try {
                socket.close()
            } catch (IOException e) {
                // Ignore IOException on close()
            }
            return port
        } catch (IOException e) {
        } finally {
            if (socket != null) {
                try {
                    socket.close()
                } catch (IOException e) {
                }
            }
        }
        throw new IllegalStateException("Could not find a free TCP/IP port to start Integration Test Server");
    }

    private static def resolveValue(Project project, IntegrationServerExtension extension, String propertyName, def defaultValue) {
        if (project.hasProperty(propertyName)) {
            project.property(propertyName)
        } else {
            def propertyValue = extension[propertyName]
            propertyValue ? propertyValue : defaultValue
        }
    }

    static IntegrationServerExtension getExtension(Project project) {
        project.extensions.getByType(IntegrationServerExtension)
    }

    static def getServerWorkingDir(Project project) {
        def serverVersion = getExtension(project).serverVersion
        def targetDir = project.buildDir.toPath().resolve(PluginUtils.DIST_DESTINATION_NAME).toAbsolutePath().toString()
        Paths.get(targetDir, "xl-deploy-${serverVersion}-server").toAbsolutePath().toString()
    }

    static IntegrationServerExtension createAndInitialize(Project project) {
        def extension = project.extensions.create(EXTENSION_NAME, IntegrationServerExtension)

        extension.serverHttpPort = resolveValue(project, extension, "serverHttpPort", findFreePort())
        extension.serverPingTotalTries = resolveValue(project, extension, "serverPingTotalTries", 60)
        extension.serverPingRetrySleepTime = resolveValue(project, extension, "serverPingRetrySleepTime", 10)
        extension.provisionSocketTimeout = resolveValue(project, extension, "provisionSocketTimeout", 6000)
        extension.akkaRemotingPort = resolveValue(project, extension, "akkaRemotingPort", findFreePort())
        extension.serverVersion = resolveValue(project, extension, "serverVersion", project.property("xlDeployVersion"))
        extension.serverContextRoot = resolveValue(project, extension, "serverContextRoot", "/")
        extension.logLevels = resolveValue(project, extension, "logLevels", new HashMap<String, String>())
        extension.overlays = resolveValue(project, extension, "overlays", new HashMap<String, List<Object>>())
        extension
    }
}
