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
            } catch (ignore) {
            }
            return port
        } catch (ignore) {
        } finally {
            if (socket != null) {
                try {
                    socket.close()
                } catch (ignore) {
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

    private static def resolveIntValue(Project project, IntegrationServerExtension extension, String propertyName, def defaultValue) {
        def value = resolveValue(project, extension, propertyName, defaultValue)
        if (value == null) {
            null as Integer
        } else Integer.parseInt(value as String)
    }

    private static def resolveBooleanValue(Project project, IntegrationServerExtension extension, String propertyName) {
        if (project.hasProperty(propertyName)) {
            def value = project.property(propertyName)
            !value || Boolean.parseBoolean(value as String)
        } else {
            def valueFromExtension = extension[propertyName]
            valueFromExtension != null ? valueFromExtension : false
        }
    }

    static IntegrationServerExtension getExtension(Project project) {
        project.extensions.getByType(IntegrationServerExtension)
    }

    static def getServerWorkingDir(Project project) {
        def serverVersion = getExtension(project).serverVersion
        def targetDir = project.buildDir.toPath().resolve(PluginUtil.DIST_DESTINATION_NAME).toAbsolutePath().toString()
        Paths.get(targetDir, "xl-deploy-${serverVersion}-server").toAbsolutePath().toString()
    }

    static create(Project project) {
        project.extensions.create(EXTENSION_NAME, IntegrationServerExtension)
    }

    static initialize(Project project) {
        def extension = getExtension(project)
        extension.serverHttpPort = resolveIntValue(project, extension, "serverHttpPort", findFreePort())
        extension.serverPingTotalTries = resolveIntValue(project, extension, "serverPingTotalTries", 60)
        extension.serverPingRetrySleepTime = resolveIntValue(project, extension, "serverPingRetrySleepTime", 10)
        extension.provisionSocketTimeout = resolveIntValue(project, extension, "provisionSocketTimeout", 6000)
        extension.akkaRemotingPort = resolveIntValue(project, extension, "akkaRemotingPort", findFreePort())
        extension.derbyPort = resolveIntValue(project, extension, "derbyPort", findFreePort())
        extension.serverDebugPort = resolveIntValue(project, extension, "serverDebugPort", null)
        extension.serverDebugSuspend = resolveBooleanValue(project, extension, "serverDebugSuspend")
        extension.logSql = resolveBooleanValue(project, extension, "logSql")
        extension.serverVersion = resolveValue(project, extension, "serverVersion", project.property("xlDeployVersion"))
        extension.serverContextRoot = resolveValue(project, extension, "serverContextRoot", "/")
        extension.xldIsDataVersion = resolveValue(project, extension, "xldIsDataVersion", project.property("xldIsDataVersion"))
        extension.logLevels = resolveValue(project, extension, "logLevels", new HashMap<String, String>())
        extension.overlays = resolveValue(project, extension, "overlays", new HashMap<String, List<Object>>())
        extension.driverVersions = resolveValue(project, extension, "driverVersions", [
            'postgres': '42.2.9',
            'mysql': '5.1.6',
            'oracle-xe-11g': '11.2.0.4',
            'mssql': '8.4.1.jre8',
            'db2': '4.19.26'
        ])
    }
}
