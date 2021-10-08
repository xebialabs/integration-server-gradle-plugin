package ai.digital.integration.server.util

import ai.digital.integration.server.IntegrationServerExtension
import ai.digital.integration.server.domain.Server
import org.gradle.api.Project
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class DeployServerUtil {

    companion object {

        @JvmStatic
        fun getHttpHost(): String {
            return "localhost"
        }

        @JvmStatic
        fun getUrl(project: Project): String {
            val server = getServer(project)
            val hostName = getHttpHost()
            return if (isTls(project)) {
                "https://$hostName:${server.httpPort}${server.contextRoot}"
            } else {
                "http://$hostName:${server.httpPort}${server.contextRoot}"
            }
        }

        @JvmStatic
        fun composeUrl(project: Project, path: String): String {
            var url = getUrl(project)
            var separator = "/"
            if (path.startsWith("/") || url.endsWith("/")) {
                separator = ""
                if (path.startsWith("/") && url.endsWith("/"))
                    url = url.removeSuffix("/")

            }
            return "$url$separator$path"
        }

        @JvmStatic
        fun isTls(project: Project): Boolean {
            return getServer(project).tls
        }

        @JvmStatic
        fun isAkkaSecured(project: Project): Boolean {
            return getServer(project).akkaSecured
        }

        @JvmStatic
        fun getServer(project: Project): Server {
            val ext = project.extensions.getByType(IntegrationServerExtension::class.java)
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

        @JvmStatic
        fun getServerWorkingDir(project: Project): String {
            val server = getServer(project)

            return when {
                isDockerBased(project) -> {
                    val workDir = getRelativePathInIntegrationServerDist(project, "deploy")
                    workDir.toAbsolutePath().toString()
                }
                server.runtimeDirectory == null -> {
                    val targetDir = getServerDistFolderPath(project).toString()
                    Paths.get(targetDir, "xl-deploy-${server.version}-server").toAbsolutePath().toString()
                }
                else -> {
                    val target = project.projectDir.toString()
                    Paths.get(target, server.runtimeDirectory).toAbsolutePath().toString()
                }
            }
        }

        @JvmStatic
        fun getRelativePathInIntegrationServerDist(project: Project, relativePath: String): Path {
            return Paths.get("${IntegrationServerUtil.getDist(project)}/${relativePath}")
        }

        @JvmStatic
        fun getServerDistFolderPath(project: Project): Path {
            return Paths.get(IntegrationServerUtil.getDist(project))
        }

        @JvmStatic
        fun isDockerBased(project: Project): Boolean {
            return !getServer(project).dockerImage.isNullOrBlank()
        }

        @JvmStatic
        private fun getServerVersion(project: Project, server: Server): String? {
            return if (project.hasProperty("xlDeployVersion"))
                project.property("xlDeployVersion").toString()
            else
                server.version
        }

        @JvmStatic
        private fun getHttpPort(project: Project, server: Server): Int {
            return if (project.hasProperty("serverHttpPort"))
                Integer.valueOf(project.property("serverHttpPort").toString())
            else
                server.httpPort
        }

        @JvmStatic
        private fun getDebugPort(project: Project, server: Server): Int? {
            return if (PropertyUtil.resolveBooleanValue(project, "debug", true)) {
                PropertyUtil.resolveIntValue(project, "serverDebugPort", server.debugPort)
            } else {
                null
            }
        }

        @JvmStatic
        fun createDebugString(debugSuspend: Boolean, debugPort: Int): String {
            val suspend = if (debugSuspend) "y" else "n"
            return "-Xrunjdwp:transport=dt_socket,server=y,suspend=${suspend},address=${debugPort}"
        }

        @JvmStatic
        fun isServerDefined(project: Project): Boolean {
            val ext = project.extensions.getByType(IntegrationServerExtension::class.java)
            return ext.servers.size > 0
        }

        @JvmStatic
        fun isDistDownloadRequired(project: Project): Boolean {
            return getServer(project).runtimeDirectory == null && !isDockerBased(project)
        }

        @JvmStatic
        fun readDeployitConfProperty(project: Project, key: String): String {
            val deployitConf = Paths.get("${getServerWorkingDir(project)}/conf/deployit.conf").toFile()
            return PropertiesUtil.readProperty(deployitConf, key)
        }

        @JvmStatic
        fun getLogDir(project: Project): File {
            return Paths.get(getServerWorkingDir(project), "log").toFile()
        }

        @JvmStatic
        fun grantPermissionsToIntegrationServerFolder(project: Project) {
            if (isDockerBased(project)) {
                val workDir = IntegrationServerUtil.getDist(project)

                File(workDir).walk().forEach {
                    FileUtil.grantRWPermissions(it)
                }
            }
        }

        @JvmStatic
        fun waitForBoot(project: Project, process: Process?) {
            val url = composeUrl(project, "/deployit/metadata/type")
            WaitForBootUtil.byPort(project, "Deploy", url, process)
        }

        @JvmStatic
        fun startServerFromClasspath(project: Project): Process {
            project.logger.lifecycle("startServerFromClasspath.")
            val server = getServer(project)

            val classpath = project.configurations.getByName(ConfigurationsUtil.DEPLOY_SERVER)
                    .filter { !it.name.endsWith("-sources.jar") }
                    .asPath
            project.logger.lifecycle("Launching Deploy Server from classpath ${classpath}.")

            val jvmArgs = mutableListOf<String>()
            jvmArgs.addAll(server.jvmArgs)
            server.debugPort?.let {
                jvmArgs.addAll(JavaUtil.debugJvmArg(project, it, server.debugSuspend))
            }

            val config = mutableMapOf(
                    "classpath" to classpath,
                    "discardIO" to (server.stdoutFileName == null),
                    "jvmArgs" to jvmArgs,
                    "mainClass" to "com.xebialabs.deployit.DeployitBootstrapper",
                    "programArgs" to listOf("-force-upgrades"),
                    "workDir" to File(getServerWorkingDir(project)),
            )

            server.stdoutFileName?.let {
                config["redirectTo"] = File("${getLogDir(project)}/${it}")
            }

            project.properties["integrationServerJVMPath"]?.let {
                config.putAll(JavaUtil.jvmPath(project, it as String))
            }

            project.logger.lifecycle("Starting integration test server on port ${server.httpPort} from runtime dir ${server.runtimeDirectory}")

            val process = JavaUtil.execJava(config)

            project.logger.lifecycle("Launched server on PID [${process.pid()}] with command [${process.info().commandLine().orElse("")}].")

            return process
        }

        @JvmStatic
        fun getDockerImageVersion(project: Project): String {
            val server = getServer(project)
            return "${server.dockerImage}:${server.version}"
        }

        @JvmStatic
        fun getDockerServiceName(project: Project): String {
            val server = getServer(project)
            return "deploy-${server.version}"
        }

        @JvmStatic
        fun getResolvedDockerFile(project: Project): Path {
            val server = getServer(project)
            val resultComposeFilePath = DockerComposeUtil.getResolvedDockerPath(project, dockerServerRelativePath())

            val serverTemplate = resultComposeFilePath.toFile()

            val configuredTemplate = serverTemplate.readText(Charsets.UTF_8)
                    .replace("DEPLOY_SERVER_HTTP_PORT", server.httpPort.toString())
                    .replace("DEPLOY_IMAGE_VERSION", getDockerImageVersion(project))
                    .replace(
                            "DEPLOY_PLUGINS_TO_EXCLUDE",
                            server.defaultOfficialPluginsToExclude.joinToString(separator = ",")
                    )
                    .replace("DEPLOY_VERSION", server.version.toString())
            serverTemplate.writeText(configuredTemplate)

            return resultComposeFilePath
        }

        @JvmStatic
        private fun dockerServerRelativePath(): String {
            return "deploy/server-docker-compose.yaml"
        }

    }
}
