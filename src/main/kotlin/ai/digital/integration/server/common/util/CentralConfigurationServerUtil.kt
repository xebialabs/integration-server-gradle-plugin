package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.domain.CentralConfigurationServer
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import org.gradle.api.Project
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

class CentralConfigurationServerUtil {
    companion object {

        fun hasCentralConfigurationServer(project: Project): Boolean {
            return DeployExtensionUtil.getExtension(project).centralConfigurationServer.isPresent
        }

        fun getCentralConfigurationServer(project: Project): CentralConfigurationServer {
            val cc = DeployExtensionUtil.getExtension(project).centralConfigurationServer.get()
            cc.version = getVersion(project, cc)
            cc.debugPort = getDebugPort(project, cc)
            return cc
        }

        private fun getDebugPort(project: Project, cc: CentralConfigurationServer): Int? {
            return if (PropertyUtil.resolveBooleanValue(project, "debug", true)) {
                PropertyUtil.resolveIntValue(project, "ccDebugPort", cc.debugPort)
            } else {
                null
            }
        }

        private fun getVersion(project: Project, cc: CentralConfigurationServer): String? {
            return if (project.hasProperty("centralConfigurationVersion")) {
                    project.property("centralConfigurationVersion").toString()
                } else if (!cc.version.isNullOrEmpty()) {
                    cc.version
                } else if (!DeployServerUtil.getServer(project).version.isNullOrEmpty()) {
                    DeployServerUtil.getServer(project).version
                } else {
                    project.logger.error("Central Configuration Server Version is not specified")
                    exitProcess(1)
                }
        }

        fun getServerPath(project: Project, cc: CentralConfigurationServer): Path {
            val targetDir = DeployServerUtil.getServerDistFolderPath(project).toString()
            return Paths.get(targetDir, "central-configuration-${cc.version}-server").toAbsolutePath()
        }

        fun getBinDir(project: Project, cc: CentralConfigurationServer): File {
            return Paths.get(getServerPath(project, cc).toString(), "bin").toFile()
        }

        fun getLogDir(project: Project, cc: CentralConfigurationServer): File {
            return Paths.get(getServerPath(project, cc).toString(), "log").toFile()
        }

        fun readDeployitConfProperty(project: Project, key: String): String {
            val deployitConf = Paths.get("${getServerPath(project, getCentralConfigurationServer(project))}/conf/deployit.conf").toFile()
            return PropertiesUtil.readProperty(deployitConf, key)
        }

        fun logFileName(): String {
            return "central-config"
        }

        fun getBaseUrl(project: Project): String {
            return "http://localhost:${readDeployitConfProperty(project, "http.port")}"
        }

    }
}
