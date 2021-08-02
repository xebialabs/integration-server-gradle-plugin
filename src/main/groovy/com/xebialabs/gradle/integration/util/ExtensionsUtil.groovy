package com.xebialabs.gradle.integration.util

import com.xebialabs.gradle.integration.IntegrationServerExtension
import org.gradle.api.Project

import java.nio.file.Paths

class ExtensionsUtil {
    static def EXTENSION_NAME = "integrationServer"

    static int findFreePort() {
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
        throw new IllegalStateException("Could not find a free TCP/IP port to start Integration Test Server")
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
        def serverRuntimeDirectory = getExtension(project).serverRuntimeDirectory
        if (serverRuntimeDirectory == null) {
            def serverVersion = getExtension(project).serverVersion
            def targetDir = project.buildDir.toPath().resolve(PluginUtil.DIST_DESTINATION_NAME).toAbsolutePath().toString()
            Paths.get(targetDir, "xl-deploy-${serverVersion}-server").toAbsolutePath().toString()
        } else {
            def target = project.projectDir.toString()
            Paths.get(target, serverRuntimeDirectory).toAbsolutePath().toString()
        }
    }


    static def getSatelliteWorkingDir(Project project) {
        def satelliteVersion = getExtension(project).satelliteVersion
        def targetDir = project.buildDir.toPath().resolve(PluginUtil.DIST_DESTINATION_NAME).toAbsolutePath().toString()
        Paths.get(targetDir, "xl-satellite-server-${satelliteVersion}").toAbsolutePath().toString()
    }

    static create(Project project) {
        project.extensions.create(EXTENSION_NAME, IntegrationServerExtension)
    }

    static initialize(Project project) {
        def extension = getExtension(project)
        extension.serverHttpPort = resolveIntValue(project, extension, "serverHttpPort", findFreePort())
        extension.serverPingTotalTries = resolveIntValue(project, extension, "serverPingTotalTries", 60)
        extension.serverPingRetrySleepTime = resolveIntValue(project, extension, "serverPingRetrySleepTime", 10)
        extension.serverJvmArgs = resolveValue(project, extension, "serverJvmArgs", ["-Xmx1024m", "-Duser.timezone=UTC"])
        extension.provisionSocketTimeout = resolveIntValue(project, extension, "provisionSocketTimeout", 6000)
        extension.derbyPort = resolveIntValue(project, extension, "derbyPort", findFreePort())
        extension.serverDebugPort = resolveIntValue(project, extension, "serverDebugPort", null)
        extension.satelliteDebugPort = resolveIntValue(project, extension, "satelliteDebugPort", null)
        extension.serverDebugSuspend = resolveBooleanValue(project, extension, "serverDebugSuspend")
        extension.satelliteDebugSuspend = resolveBooleanValue(project, extension, "satelliteDebugSuspend")
        extension.logSql = resolveBooleanValue(project, extension, "logSql")
        extension.serverVersion = resolveValue(project, extension, "serverVersion", project.hasProperty("xlDeployVersion") ? project.property("xlDeployVersion") : null)
        extension.serverContextRoot = resolveValue(project, extension, "serverContextRoot", "/")
        extension.xldIsDataVersion = resolveValue(project, extension, "xldIsDataVersion", project.hasProperty("xldIsDataVersion") ? project.property("xldIsDataVersion") : null)
        extension.satelliteVersion = resolveValue(project, extension, "satelliteVersion", project.hasProperty("xlDeployVersion") ? project.property("xlDeployVersion") : null)
        extension.satelliteOverlays = resolveValue(project, extension, "satelliteOverlays", new HashMap<String, List<Object>>())
        extension.logLevels = resolveValue(project, extension, "logLevels", new HashMap<String, String>())
        extension.overlays = resolveValue(project, extension, "overlays", new HashMap<String, List<Object>>())
        extension.driverVersions = resolveValue(project, extension, "driverVersions", [
                'postgres-10'     : '42.2.9',
                'postgres-12'  : '42.2.23',
                'mysql'        : '8.0.22',
                'mysql-8'      : '8.0.22',
                'oracle-19c-se': '21.1.0.0',
                'mssql'        : '8.4.1.jre8',
        ])
        extension.mqDriverVersions = resolveValue(project, extension, "mqDriverVersions", [
                'activemq': '5.16.2',
                'rabbitmq': '2.2.0'
        ])
        extension.workerRemotingPort = resolveIntValue(project, extension, "workerRemotingPort", findFreePort())
        extension.workerName = resolveValue(project, extension, "workerName", "worker-1-work")
        extension.workerDebugPort = resolveIntValue(project, extension, "workerDebugPort", null)
        extension.workerJvmArgs = resolveValue(project, extension, "workerJvmArgs", ["-Xmx1024m", "-Duser.timezone=UTC"])
        extension.serverRuntimeDirectory = resolveValue(project, extension, "serverRuntimeDirectory", null)
        extension.provisionScript = resolveValue(project, extension, "provisionScript", null)
        extension.yamlPatches = resolveValue(project, extension, "yamlPatches", new HashMap<String, Map<String, String>>())
    }
}
