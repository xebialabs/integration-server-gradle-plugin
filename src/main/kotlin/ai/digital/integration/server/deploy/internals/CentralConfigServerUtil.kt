package ai.digital.integration.server.deploy.internals

import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.util.PropertyUtil
import ai.digital.integration.server.common.util.WaitForBootUtil
import ai.digital.integration.server.deploy.domain.CentralConfigServer
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

class CentralConfigServerUtil {
    companion object {
        fun hasCentralConfigServer(project: Project): Boolean {
            return DeployExtensionUtil.getExtension(project).centralConfigServer.get().enable
        }
        fun getCC(project: Project): CentralConfigServer {
            val cc = DeployExtensionUtil.getExtension(project).centralConfigServer.get()
            cc.version = getCCVersion(project, cc)
            cc.debugPort = getDebugPort(project, cc)
            return cc
        }

        private fun getDebugPort(project: Project, cc: CentralConfigServer): Int? {
            return if (PropertyUtil.resolveBooleanValue(project, "debug", true)) {
                PropertyUtil.resolveIntValue(project, "ccDebugPort", cc.debugPort)
            } else {
                null
            }
        }

        private fun getCCVersion(project: Project, cc: CentralConfigServer): String? {
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

        private fun getCCServerPath(project: Project, cc: CentralConfigServer): Path {
            val targetDir = DeployServerUtil.getServerDistFolderPath(project).toString()
            return Paths.get(targetDir, "central-configuration-${cc.version}-server").toAbsolutePath()
        }

        fun getBinDir(project: Project, cc: CentralConfigServer): File {
            return Paths.get(getCCServerPath(project, cc).toString(), "bin").toFile()
        }

        fun getCCLog(project: Project, cc: CentralConfigServer): File {
            return Paths.get(getCCServerPath(project, cc).toString(), "log", logFileName()).toFile()
        }

        fun logFileName(): String {
            return "central-config"
        }

        fun prepare(project: Project) {
            val cc = getCC(project)
            val server = DeployServerUtil.getServer(project)
            project.logger.lifecycle("Preparing Central configuration standalone server ${cc.version} before launching it.")
            copyConfFromServer(project, server, cc)
            copyCentralConfigDir(project, server, cc)
        }

        private fun copyCentralConfigDir(project: Project, server: Server, cc: CentralConfigServer) {
            project.logger.lifecycle("Copying CC directory from ${server.name} to CC standalone")
            val sourceDir = Paths.get(DeployServerUtil.getServerWorkingDir(project), "centralConfiguration").toFile()
            val destinationDir = Paths.get(getCCServerPath(project, cc).toString(), "centralConfiguration").toFile()
            FileUtils.copyDirectory(sourceDir, destinationDir)
        }

        private fun copyConfFromServer(project: Project, server: Server, cc: CentralConfigServer) {
            project.logger.lifecycle("Copying deployit.conf from ${server.name} to CC standalone")

            val sourceDir = Paths.get(DeployServerUtil.getServerWorkingDir(project), "conf/deployit.conf").toFile()
            val destinationDir = Paths.get(getCCServerPath(project, cc).toString(), "conf").toFile()

            FileUtils.copyFileToDirectory(sourceDir, destinationDir)
            val deployConf = Paths.get(destinationDir.absolutePath, "deployit.conf").toFile()
            val replaceContent = deployConf.readText(Charsets.UTF_8)
                    .replace("http.port=${server.httpPort}", "http.port=${cc.httpPort.toString()}")
            deployConf.writeText(replaceContent)
        }
    }
}
