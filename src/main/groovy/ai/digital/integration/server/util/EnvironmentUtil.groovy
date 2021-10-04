package ai.digital.integration.server.util

import ai.digital.integration.server.domain.Cli
import ai.digital.integration.server.domain.Server
import org.gradle.api.Project

class EnvironmentUtil {

    static def getServerEnv(Project project, Server server) {
        getEnv(project, "DEPLOYIT_SERVER_OPTS", server.debugSuspend, server.debugPort, null)
    }

    static def getCliEnv(Project project, Cli cli, Map<String, String> extraParams, List<File> extraClassPath) {
        def env = getEnv(project, "DEPLOYIT_CLI_OPTS", cli.debugSuspend, cli.debugPort, null, extraParams)
        env.put("EXTRA_DEPLOYIT_CLI_CLASSPATH", extraClassPath.join(OsUtil.getPathSeparator()))
        env
    }

    static def getEnv(Project project, String variableName, Boolean debugSuspend, Integer debugPort, String logFileName) {
        getEnv(project, variableName, debugSuspend, debugPort, logFileName, [:])
    }

    static def getEnv(Project project, String variableName, Boolean debugSuspend, Integer debugPort, String logFileName, Map<String, String> extraProps) {
        def opts = logFileName ?
                "-Xmx1024m -DLOGFILE=$logFileName" : "-Xmx1024m"
        if (debugPort != null) {
            opts = "${opts} ${ServerUtil.createDebugString(debugSuspend, debugPort)} "
        }

        if (ServerUtil.isTls(project)) {
            def tls = SslUtil.getTls(project, DeployServerUtil.getServerWorkingDir(project))
            opts = "${opts} -Djavax.net.ssl.trustStore=${tls.trustStoreFile()} -Djavax.net.ssl.trustStorePassword=$tls.truststorePassword "
        }

        opts += extraProps
                .findAll { it -> it.value != null }
                .collect { it -> "-D${it.key}=${it.value}" }
                .join(" ")
        [(variableName): opts.toString()]
    }
}
