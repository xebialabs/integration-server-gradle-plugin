package com.xebialabs.gradle.integration.util

import com.xebialabs.gradle.integration.domain.Server

class EnvironmentUtil {

    static def getServerEnv(Server server) {
        return getEnv("DEPLOYIT_SERVER_OPTS", server.debugSuspend, server.debugPort, null)
    }

    static def getEnv(String variableName, Boolean debugSuspend, Integer debugPort, String logFileName) {
        def opts = logFileName ?
                "-Xmx1024m -DLOGFILE=$logFileName} -Djava.util.logging.manager=org.jboss.logmanager.LogManager" :
                "-Xmx1024m -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
        def suspend = debugSuspend ? 'y' : 'n'
        if (debugPort != null) {
            opts = "${opts} -agentlib:jdwp=transport=dt_socket,server=y,suspend=${suspend},address=${debugPort} "
        }
        [(variableName): opts.toString()]
    }
}
