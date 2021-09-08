package ai.digital.integration.server.util

import ai.digital.integration.server.domain.Cli
import ai.digital.integration.server.domain.Server

class EnvironmentUtil {

    static def getServerEnv(Server server) {
        getEnv("DEPLOYIT_SERVER_OPTS", server.debugSuspend, server.debugPort, null)
    }

    static def getCliEnv(Cli cli, Map<String, String> extraParams, List<File> extraClassPath) {
        def env = getEnv("DEPLOYIT_CLI_OPTS", cli.debugSuspend, cli.debugPort, null, extraParams)
        env.put("EXTRA_DEPLOYIT_CLI_CLASSPATH", extraClassPath.join(OsUtil.pathSeparator))
        env
    }

    static def getEnv(String variableName, Boolean debugSuspend, Integer debugPort, String logFileName) {
        getEnv(variableName, debugSuspend, debugPort, logFileName, [:])
    }

    static def getEnv(String variableName, Boolean debugSuspend, Integer debugPort, String logFileName, Map<String, String> extraProps) {
        def opts = logFileName ?
                "-Xmx1024m -DLOGFILE=$logFileName" : "-Xmx1024m"
        if (debugPort != null) {
            opts = "${opts} ${ServerUtil.createDebugString(debugSuspend, debugPort)} "
        }
        opts += extraProps
                .findAll { it -> it.value != null }
                .collect { it -> "-D${it.key}=${it.value}" }
                .join(" ")
        [(variableName): opts.toString()]
    }
}
