package ai.digital.integration.server.release.util

import ai.digital.integration.server.common.domain.Cluster
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.util.*
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import ai.digital.integration.server.release.ReleaseIntegrationServerExtension
import org.gradle.api.Project
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class ReleaseServerUtil {
    companion object {

        private fun getHttpHost(): String {
            return "localhost"
        }

        private fun getUrl(project: Project): String {
            val server = getServer(project)
            val hostName = getHttpHost()
            return if (isTls(project)) {
                "https://$hostName:${server.httpPort}${server.contextRoot}"
            } else {
                "http://$hostName:${server.httpPort}${server.contextRoot}"
            }
        }

        private fun composeUrl(project: Project, path: String): String {
            var url = getUrl(project)
            var separator = "/"
            if (path.startsWith("/") || url.endsWith("/")) {
                separator = ""
                if (path.startsWith("/") && url.endsWith("/"))
                    url = url.removeSuffix("/")

            }
            return "$url$separator$path"
        }

        fun isTls(project: Project): Boolean {
            return getServer(project).tls
        }

        fun getCluster(project: Project): Cluster {
            return DeployExtensionUtil.getExtension(project).cluster.get()
        }

        fun isClusterEnabled(project: Project): Boolean {
            return getCluster(project).enable
        }

        fun getConfDir(project: Project): File {
            return Paths.get(getServerWorkingDir(project), "conf").toFile()
        }

        fun readReleaseServerConfProperty(project: Project, key: String): String {
            val deployitConf = Paths.get("${getServerWorkingDir(project)}/conf/xl-release-server.conf").toFile()
            return PropertiesUtil.readProperty(deployitConf, key)
        }

        fun getServer(project: Project): Server {
            val ext = project.extensions.getByType(ReleaseIntegrationServerExtension::class.java)
            val server = ext.servers.first()
            server.debugPort = getDebugPort(project, server)
            server.httpPort = getHttpPort(project, server)
            server.version = getServerVersion(project, server)

            server.dockerImage?.let {
                server.runtimeDirectory = null
            }

            if (!server.contextRoot.startsWith("/")) {
                server.contextRoot = "/$server.contextRoot"
            }

            return server
        }

        fun getServerWorkingDir(project: Project): String {
            val workDir = getRelativePathInIntegrationServerDist(project, "release")
            return workDir.toAbsolutePath().toString()
        }

        private fun getRelativePathInIntegrationServerDist(project: Project, relativePath: String): Path {
            return Paths.get("${IntegrationServerUtil.getDist(project)}/${relativePath}")
        }

        fun isDockerBased(project: Project): Boolean {
            return !getServer(project).dockerImage.isNullOrBlank()
        }

        private fun getServerVersion(project: Project, server: Server): String? {
            return if (project.hasProperty("xlReleaseVersion"))
                project.property("xlReleaseVersion").toString()
            else
                server.version
        }

        private fun getHttpPort(project: Project, server: Server): Int {
            return if (project.hasProperty("serverHttpPort"))
                Integer.valueOf(project.property("serverHttpPort").toString())
            else
                server.httpPort
        }

        private fun getDebugPort(project: Project, server: Server): Int? {
            return if (PropertyUtil.resolveBooleanValue(project, "debug", true)) {
                PropertyUtil.resolveIntValue(project, "serverDebugPort", server.debugPort)
            } else {
                null
            }
        }

        fun grantPermissionsToIntegrationServerFolder(project: Project) {
            if (isDockerBased(project)) {
                val workDir = IntegrationServerUtil.getDist(project)

                File(workDir).walk().forEach {
                    FileUtil.grantRWPermissions(it)
                }
            }
        }

        fun isReleaseServerDefined(project: Project): Boolean {
            val ext = project.extensions.getByType(ReleaseIntegrationServerExtension::class.java)
            return ext.servers.size > 0
        }

        fun waitForBoot(project: Project, process: Process?) {
            val url = composeUrl(project, "/api/extension/metadata")
            val server = getServer(project)
            WaitForBootUtil.byPort(project, "Release", url, process, server.pingRetrySleepTime, server.pingTotalTries)
        }

        fun getDockerImageVersion(project: Project): String {
            val server = getServer(project)
            return "${server.dockerImage}:${server.version}"
        }

        fun getDockerServiceName(project: Project): String {
            val server = getServer(project)
            return "release-${server.version}"
        }

        fun runDockerBasedInstance(project: Project) {
            project.exec {
                executable = "docker-compose"
                args = listOf("-f", getResolvedDockerFile(project).toFile().toString(), "up", "-d")
            }
        }

        fun getResolvedDockerFile(project: Project): Path {
            val server = getServer(project)
            val resultComposeFilePath = DockerComposeUtil.getResolvedDockerPath(project, dockerServerRelativePath())

            val serverTemplate = resultComposeFilePath.toFile()

            val configuredTemplate = serverTemplate.readText(Charsets.UTF_8)
                .replace("RELEASE_SERVER_HTTP_PORT", server.httpPort.toString())
                .replace("RELEASE_IMAGE_VERSION", getDockerImageVersion(project))
                .replace(
                    "RELEASE_PLUGINS_TO_EXCLUDE",
                    server.defaultOfficialPluginsToExclude.joinToString(separator = ",")
                )
                .replace("RELEASE_VERSION", server.version.toString())
            serverTemplate.writeText(configuredTemplate)

            return resultComposeFilePath
        }

        private fun dockerServerRelativePath(): String {
            return "release/server-docker-compose.yaml"
        }

    }
}
