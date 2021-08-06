package com.xebialabs.gradle.integration.util

import org.gradle.api.Project

class EnvironmentUtil {

    static def getEnv(Project project, String variableName) {
        def extension = ExtensionsUtil.getExtension(project)
        return getEnv(variableName, extension.serverDebugSuspend, extension.serverDebugPort, null)
    }

    static def getEnv(String variableName, Boolean debugSuspend, Integer debugPort, String logFileName) {
        def opts = logFileName ?
                "-Xmx1024m -DLOGFILE=$logFileName} -Djava.util.logging.manager=org.jboss.logmanager.LogManager" :
                "-Xmx1024m -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
        def suspend = debugSuspend ? 'y' : 'n'
        if (debugPort != null) {
            opts = "${opts} -agentlib:jdwp=transport=dt_socket,server=y,suspend=${suspend},address=${debugPort} "
        }
        [${variableName}: opts.toString()]
    }
}
