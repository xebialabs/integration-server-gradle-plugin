package ai.digital.integration.server.deploy.util

import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.util.*
import org.gradle.api.Project
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class DeployServerUtil {
    companion object {

        private const val dockerServerRelativePath = "deploy/server-docker-compose.yaml"

        fun getHttpHost(): String {
            return "localhost"
        }

        fun getUrl(project: Project): String {
            val server = getServer(project)
            val hostName = getHttpHost()
            return if (isTls(project)) {
                "https://$hostName:${server.httpPort}${server.contextRoot}"
            } else {
                "http://$hostName:${server.httpPort}${server.contextRoot}"
            }
        }

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

        fun isTls(project: Project): Boolean {
            return getServer(project).tls
        }

        fun isAkkaSecured(project: Project): Boolean {
            return getServer(project).akkaSecured
        }

        fun getServer(project: Project): Server {
            val server = DeployExtensionUtil.getExtension(project).servers.first()
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
            val server = getServer(project)

            return when {
                isDockerBased(project) -> {
                    val workDir = ServerUtil.getRelativePathInIntegrationServerDist(project, "deploy")
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

        fun getServerDistFolderPath(project: Project): Path {
            return Paths.get(IntegrationServerUtil.getDist(project))
        }

        fun isDockerBased(project: Project): Boolean {
            return !getServer(project).dockerImage.isNullOrBlank()
        }


        private fun getServerVersion(project: Project, server: Server): String? {
            return if (!server.version.isNullOrBlank()) {
                return server.version
            } else if (project.hasProperty("xlDeployVersion"))
                project.property("xlDeployVersion").toString()
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

        fun createDebugString(debugSuspend: Boolean, debugPort: Int): String {
            val suspend = if (debugSuspend) "y" else "n"
            return "-Xrunjdwp:transport=dt_socket,server=y,suspend=${suspend},address=${debugPort}"
        }

        fun isDeployServerDefined(project: Project): Boolean {
            return DeployExtensionUtil.getExtension(project).servers.size > 0
        }

        fun isDistDownloadRequired(project: Project): Boolean {
            return getServer(project).runtimeDirectory == null && !isDockerBased(project)
        }

        fun readDeployitConfProperty(project: Project, key: String): String {
            val deployitConf = Paths.get("${getServerWorkingDir(project)}/conf/deployit.conf").toFile()
            return PropertiesUtil.readProperty(deployitConf, key)
        }

        fun getLogDir(project: Project): File {
            return Paths.get(getServerWorkingDir(project), "log").toFile()
        }

        fun grantPermissionsToIntegrationServerFolder(project: Project) {
            if (isDockerBased(project)) {
                val workDir = IntegrationServerUtil.getDist(project)

                File(workDir).walk().forEach {
                    FileUtil.grantRWPermissions(it)
                }
            }
        }

        fun waitForBoot(project: Project, process: Process?) {
            val url = composeUrl(project, "/deployit/metadata/type")
            val server = getServer(project)
            WaitForBootUtil.byPort(project, "Deploy", url, process, server.pingRetrySleepTime, server.pingTotalTries)
        }

        fun startServerFromClasspath(project: Project): Process {
            project.logger.lifecycle("startServerFromClasspath.")
            val server = getServer(project)

            val classpath = project.configurations.getByName(DeployConfigurationsUtil.DEPLOY_SERVER)
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

            project.logger.lifecycle("Launched server on PID [${process.pid()}] with command [${
                process.info().commandLine().orElse("")
            }].")

            return process
        }

        fun getDockerImageVersion(project: Project): String {
            val server = getServer(project)
            return "${server.dockerImage}:${server.version}"
        }

        fun getDockerServiceName(project: Project): String {
            val server = getServer(project)
            return "deploy-${server.version}"
        }

        fun getResolvedDockerFile(project: Project): Path {
            val server = getServer(project)
            val resultComposeFilePath = DockerComposeUtil.getResolvedDockerPath(project, dockerServerRelativePath)

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
    }
}
