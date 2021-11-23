package ai.digital.integration.server.deploy.internals

import ai.digital.integration.server.deploy.domain.Cli
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.util.OsUtil
import ai.digital.integration.server.common.util.TlsUtil
import ai.digital.integration.server.deploy.domain.Permission
import org.gradle.api.Project
import java.io.File

class EnvironmentUtil {
    companion object {
        fun getServerEnv(project: Project,server: Server): MutableMap<String, String> {
            return getEnv(project, "JDK_JAVA_OPTIONS", server.debugSuspend, server.debugPort, null)
        }

        fun getCliEnv(project: Project, cli: Cli, extraParams: Map<String, String?>, extraClassPath: List<File>): Map<String, String> {
            val env = getEnv(project, "JDK_JAVA_OPTIONS", cli.debugSuspend, cli.debugPort, null, extraParams)
            env["EXTRA_DEPLOYIT_CLI_CLASSPATH"] = extraClassPath.joinToString(separator = OsUtil.getPathSeparator())
            return env
        }

        fun getPermissionServiceEnv(project: Project,server: Permission): MutableMap<String, String> {
            return getEnv(project, "JDK_JAVA_OPTIONS", server.debugSuspend, server.debugPort, null)
        }

        fun getEnv(
            project: Project,
            variableName: String,
            debugSuspend: Boolean,
            debugPort: Int?,
            logFileName: String?
        ): MutableMap<String, String> {
            return getEnv(project, variableName, debugSuspend, debugPort, logFileName, mutableMapOf())
        }

        fun getEnv(
            project: Project,
            variableName: String,
            debugSuspend: Boolean,
            debugPort: Int?,
            logFileName: String?,
            extraProps: Map<String, String?>
        ): MutableMap<String, String> {
            var opts = if (!logFileName.isNullOrEmpty()) "-Xmx1024m -DLOGFILE=\"$logFileName\"" else "-Xmx1024m"

            debugPort?.let {
                opts = "$opts ${DeployServerUtil.createDebugString(debugSuspend, it)} "
            }

            if (DeployServerUtil.isTls(project)) {
                val tls = TlsUtil.getTls(project, DeployServerUtil.getServerWorkingDir(project))
                opts = "$opts -Djavax.net.ssl.trustStore=\"${tls?.trustStoreFile()}\" -Djavax.net.ssl.trustStorePassword=\"${tls?.truststorePassword}\" "
            }

            opts += extraProps
                .filterValues { it != null }
                .map { "-D${it.key}=\"${it.value}\"" }
                .joinToString(separator = " ")

            return mutableMapOf(
                variableName to opts
            )
        }
    }
}
