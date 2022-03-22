package ai.digital.integration.server.deploy.internals

import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.util.*
import ai.digital.integration.server.deploy.domain.CentralConfigurationStandalone
import ai.digital.integration.server.deploy.tasks.cluster.ClusterConstants
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

class CentralConfigurationStandaloneUtil {
    companion object {
        fun hasCC(project: Project): Boolean {
            return DeployExtensionUtil.getExtension(project).CentralConfigurationStandalone.get().enable
        }

        fun getCC(project: Project): CentralConfigurationStandalone {
            val cc = DeployExtensionUtil.getExtension(project).CentralConfigurationStandalone.get()
            cc.version = getCCVersion(project, cc)
            cc.debugPort = getDebugPort(project, cc)
            return cc
        }

        private fun getDebugPort(project: Project, cc: CentralConfigurationStandalone): Int? {
            return if (PropertyUtil.resolveBooleanValue(project, "debug", true)) {
                PropertyUtil.resolveIntValue(project, "ccDebugPort", cc.debugPort)
            } else {
                null
            }
        }

        private fun getCCVersion(project: Project, cc: CentralConfigurationStandalone): String? {
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

        fun isDockerBased(project: Project): Boolean {
            return isDockerBased(getCC(project))
        }

        fun isDockerBased(cc: CentralConfigurationStandalone): Boolean {
            return !cc.dockerImage.isNullOrBlank()
        }

        fun getCCServerPath(project: Project, cc: CentralConfigurationStandalone): Path {
            val targetDir = DeployServerUtil.getServerDistFolderPath(project).toString()
            return Paths.get(targetDir, "central-configuration-${cc.version}-server").toAbsolutePath()
        }

        fun getBinDir(project: Project, cc: CentralConfigurationStandalone): File {
            return Paths.get(getCCServerPath(project, cc).toString(), "bin").toFile()
        }

        fun getLogDir(project: Project, cc: CentralConfigurationStandalone): File {
            return Paths.get(getCCServerPath(project, cc).toString(), "log").toFile()
        }

        fun prepare(project: Project) {
            val cc = getCC(project)
            val server = DeployServerUtil.getServer(project)
            project.logger.lifecycle("Preparing Central configuration standalone server ${cc.version} before launching it.")
            copyConfFromServer(project, server, cc)
            copyCentralConfigDir(project, server, cc)
        }

        private fun copyCentralConfigDir(project: Project, server: Server, cc: CentralConfigurationStandalone) {
            project.logger.lifecycle("Copying CC directory from ${server.name} to CC standalone")
            val sourceDir = Paths.get(DeployServerUtil.getServerWorkingDir(project), "centralConfiguration").toFile()
            val destinationDir = Paths.get(getCCServerPath(project, cc).toString(), "centralConfiguration").toFile()
            FileUtils.copyDirectory(sourceDir, destinationDir)
        }

        private fun copyConfFromServer(project: Project, server: Server, cc: CentralConfigurationStandalone) {
            project.logger.lifecycle("Copying deployit.conf from ${server.name} to CC standalone")

            val sourceDir = Paths.get(DeployServerUtil.getServerWorkingDir(project), "conf/deployit.conf").toFile()
            val destinationDir = Paths.get(getCCServerPath(project, cc).toString(), "conf").toFile()

            FileUtils.copyFileToDirectory(sourceDir, destinationDir)
            val deployConf = Paths.get(destinationDir.absolutePath, "deployit.conf").toFile()
            val replaceContent = deployConf.readText(Charsets.UTF_8)
                    .replace("http.port=${server.httpPort}", "http.port=${cc.httpPort.toString()}")
            deployConf.writeText(replaceContent)
        }

        fun readServerKey(project: Project, key: String): String {
            val cc = getCC(project)
            val deployitConf = Paths.get("${getCCServerPath(project, cc)}/conf/deployit.conf").toFile()
            return PropertiesUtil.readProperty(deployitConf, key)
        }

        fun getResolvedDockerFile(project: Project, cc: CentralConfigurationStandalone): Path {
            val dockerComposeStream = {}::class.java.classLoader.getResourceAsStream(dockerCcRelativePath())
            val newPath = "cc-${cc.version}/docker-compose-cc.yaml"
            val destinationPath = IntegrationServerUtil.getRelativePathInIntegrationServerDist(project, newPath)
            dockerComposeStream?.let {
                FileUtil.copyFile(it, destinationPath)
            }
            val resultComposeFilePath = DockerComposeUtil.getResolvedDockerPath(project, newPath)

            val serverTemplate = resultComposeFilePath.toFile()

            val configuredTemplate = serverTemplate.readText(Charsets.UTF_8)
                .replace("{{CC_IMAGE_VERSION}}", getDockerImageVersion(cc))

            serverTemplate.writeText(configuredTemplate)

            return resultComposeFilePath
        }

        fun getDockerImageVersion(cc: CentralConfigurationStandalone): String {
            return "${cc.dockerImage}:${cc.version}"
        }

        private fun dockerCcRelativePath(): String {
            return "central-conf/docker-compose-cc.yaml"
        }

        fun runDockerBasedInstance(project: Project, cc: CentralConfigurationStandalone) {
            project.exec {
                executable = "docker-compose"
                args = listOf("-f", getResolvedDockerFile(project, cc).toFile().toString(), "up", "-d")
            }
        }

        fun networkExists(project: Project): Boolean {
            return DockerUtil.execute(
                project,
                listOf("network",
                    "ls",
                    "--filter",
                    "name=^${ClusterConstants.NETWORK_NAME}$",
                    "--format=\"{{ .Name }}\"")
            ).isNotBlank()
        }

        fun createNetwork(project: Project) {
            if (!networkExists(project)) {
                project.exec {
                    executable = "docker"
                    args = listOf("network", "create", ClusterConstants.NETWORK_NAME)
                }
            }
        }

    }
}
