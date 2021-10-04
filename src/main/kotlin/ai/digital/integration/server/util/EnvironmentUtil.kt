package ai.digital.integration.server.util

import ai.digital.integration.server.domain.Cli
import ai.digital.integration.server.domain.Server
import java.io.File

class EnvironmentUtil {
    companion object {
        @JvmStatic
        fun getServerEnv(server: Server): MutableMap<String, String> {
            return getEnv("DEPLOYIT_SERVER_OPTS", server.debugSuspend, server.debugPort, null)
        }

        @JvmStatic
        fun getCliEnv(cli: Cli, extraParams: Map<String, String>, extraClassPath: List<File>): Map<String, String> {
            val env = getEnv("DEPLOYIT_CLI_OPTS", cli.debugSuspend, cli.debugPort, null, extraParams)
            env["EXTRA_DEPLOYIT_CLI_CLASSPATH"] = extraClassPath.joinToString(separator = OsUtil.getPathSeparator())
            return env
        }

        @JvmStatic
        fun getEnv(
            variableName: String,
            debugSuspend: Boolean,
            debugPort: Int?,
            logFileName: String?
        ): MutableMap<String, String> {
            return getEnv(variableName, debugSuspend, debugPort, logFileName, mutableMapOf())
        }

        @JvmStatic
        fun getEnv(
            variableName: String,
            debugSuspend: Boolean,
            debugPort: Int?,
            logFileName: String?,
            extraProps: Map<String, String>
        ): MutableMap<String, String> {
            var opts = if (!logFileName.isNullOrEmpty()) "-Xmx1024m -DLOGFILE=$logFileName" else "-Xmx1024m"

            debugPort?.let {
                opts = "$opts ${DeployServerUtil.createDebugString(debugSuspend, it)} "
            }

            opts += extraProps
                .filter { true }
                .map { "-D${it.key}=${it.value}" }
                .joinToString(separator = " ")

            return mutableMapOf(
                variableName to opts
            )
        }
    }
}
