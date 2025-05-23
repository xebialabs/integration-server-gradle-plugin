package ai.digital.integration.server.deploy.internals

import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.Cluster
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.util.*
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

class DeployServerUtil {
    companion object {

        fun isTls(project: Project): Boolean {
            return getServer(project).tls
        }

        fun isPekkoSecured(project: Project): Boolean {
            return getServer(project).pekkoSecured
        }

        private fun isNoLicense(project: Project): Boolean {
            return getServer(project).noLicense
        }

        fun getServer(project: Project): Server {
            return enrichServer(project,
                DeployExtensionUtil.getExtension(project).servers.first { server ->
                    !server.previousInstallation
                })
        }

        fun getServers(project: Project): List<Server> {
            return DeployExtensionUtil.getExtension(project).servers.map { server: Server ->
                enrichServer(project, server)
            }
        }

        fun getPreviousInstallationServer(project: Project): Server {
            return DeployExtensionUtil.getExtension(project).servers.first { server -> server.previousInstallation }
        }

        fun isPreviousInstallationServerDefined(project: Project): Boolean {
            return DeployExtensionUtil.getExtension(project).servers.find { server -> server.previousInstallation } != null
        }

        private fun enrichServer(project: Project, server: Server): Server {
            server.debugPort = getDebugPort(project, server)
            server.httpPort = getHttpPort(project, server)
            server.version = getServerVersion(project, server)

            if (isPreviousInstallationServerDefined(project)) {
                server.httpPort = getPreviousInstallationServer(project).httpPort
            }

            server.dockerImage?.let {
                server.runtimeDirectory = null
            }
            if (!server.contextRoot.startsWith("/")) {
                server.contextRoot = "/$server.contextRoot"
            }
            return server
        }

        fun getServerWorkingDir(project: Project): String {
            return getServerWorkingDir(project, getServer(project))
        }

        fun getServerWorkingDir(project: Project, server: Server): String {
            return when {
                isDockerBased(server) -> {
                    val workDir = IntegrationServerUtil.getRelativePathInIntegrationServerDist(project,
                        "deploy-${server.version}")
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
            return isDockerBased(getServer(project))
        }

        fun isDockerBased(server: Server): Boolean {
            return !server.dockerImage.isNullOrBlank()
        }

        fun getCluster(project: Project): Cluster {
            return DeployExtensionUtil.getExtension(project).cluster.get()
        }

        fun isClusterEnabled(project: Project): Boolean {
            return getCluster(project).enable
        }

        fun clusterMode(project: Project): String {
            var mode =  "default"
            if (project.hasProperty("clusterMode")) {
                mode = project.property("clusterMode").toString()
                if (mode != "full" && mode != "hot-standby" && mode != "default")
                    throw GradleException("full, hot-standby, default are only valid for cluster mode property")
            }
            return mode
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

        fun isDistDownloadRequired(project: Project, server: Server): Boolean {
            return server.runtimeDirectory == null && !isDockerBased(project)
        }

        fun readDeployitConfProperty(project: Project, key: String): String {
            val deployitConf = Paths.get("${getServerWorkingDir(project)}/conf/deployit.conf").toFile()
            return PropertiesUtil.readProperty(deployitConf, key)
        }

        fun getLogDir(project: Project, server: Server): File {
            val logDir = Paths.get(getServerWorkingDir(project, server), "log").toFile()
            logDir.mkdirs()
            return logDir
        }

        fun getLogDir(project: Project): File {
            val server = getServer(project)
            return getLogDir(project, server)
        }

        fun getConfDir(project: Project): File {
            val server = getServer(project)
            return Paths.get(getServerWorkingDir(project, server), "conf").toFile()
        }

        fun grantPermissionsToIntegrationServerFolder(project: Project) {
            if (isDockerBased(project)) {
                val workDir = IntegrationServerUtil.getDist(project)

                File(workDir).walk().forEach {
                    FileUtil.grantRWPermissions(it)
                }
            }
        }

        fun waitForBoot(project: Project, process: Process?, server: Server, auxiliaryServer: Boolean = false) {
            val clusterEnabled = isClusterEnabled(project)
            fun saveLogs(lastUpdate: LocalDateTime): LocalDateTime {
                if (isDockerBased(server) || clusterEnabled) {
                    saveServerLogsToFile(project, server, "deploy-${server.version}", lastUpdate)
                }
                return LocalDateTime.now()
            }
            val url = if (isNoLicense(project)){
                EntryPointUrlUtil(project, ProductName.DEPLOY).composeUrl("/productregistration", auxiliaryServer)
            } else {
                EntryPointUrlUtil(project, ProductName.DEPLOY).composeUrl("/deployit/metadata/type", auxiliaryServer)
            }

            val lastLogUpdate = WaitForBootUtil.byPort(project,
                "Deploy",
                url,
                process,
                server.pingRetrySleepTime,
                server.pingTotalTries) {
                saveLogs(it)
            }
            saveLogs(lastLogUpdate)
        }

        fun saveServerLogsToFile(project: Project, server: Server, containerName: String, lastUpdate: LocalDateTime) {
            val logContent = DockerUtil.dockerLogs(project, containerName, lastUpdate)
            val logDir = getLogDir(project, server)
            File(logDir, "$containerName.log").writeText(logContent, StandardCharsets.UTF_8)
        }

        fun writeArgsToFile(args: List<String>, prefix: String): File {
            val tempFile = File.createTempFile(prefix, ".args")
            tempFile.writeText(args.joinToString(separator = System.lineSeparator()))
            tempFile.deleteOnExit()
            return tempFile
        }

        fun startServerFromClasspath(project: Project): Process {
            project.logger.lifecycle("startServerFromClasspath.")
            val server = getServer(project)

            val classpath = project.configurations.getByName(DeployConfigurationsUtil.DEPLOY_SERVER)
                .filter { !it.name.endsWith("-sources.jar") }
                .asPath
            project.logger.lifecycle("Launching Deploy Server from classpath ${classpath}.")


            // Prepare JVM args list including the classpath argument
            val jvmArgs = mutableListOf<String>()
            jvmArgs.add("-classpath")
            jvmArgs.add(classpath)
            jvmArgs.addAll(server.jvmArgs)
            server.debugPort?.let {
                jvmArgs.addAll(JavaUtil.debugJvmArg(project, it, server.debugSuspend))
            }

            val jvmArgsFile = writeArgsToFile(jvmArgs, "jvmArgs")


            val config = mutableMapOf(
                "discardIO" to (server.stdoutFileName == null),
                "jvmArgs" to listOf("@${jvmArgsFile.absolutePath}"),
                "mainClass" to "com.xebialabs.deployit.DeployitBootstrapper",
                "programArgs" to listOf("-force-upgrades"),
                "workDir" to File(getServerWorkingDir(project))
            )

            server.stdoutFileName?.let {
                config["redirectTo"] = File("${getLogDir(project, server)}/${it}")
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

        fun getDockerImageVersion(server: Server): String {
            return "${server.dockerImage}:${server.version}"
        }

        fun getDockerServiceName(server: Server): String {
            return "deploy-${server.version}"
        }

        private fun getOldDockerServerPath(project: Project): String {
            if (isPreviousInstallationServerDefined(project)) {
                val rootPath = IntegrationServerUtil.getDist(project)
                val oldDockerServer =
                    DeployExtensionUtil.getExtension(project).servers.first { oldServer -> oldServer.previousInstallation }
                return rootPath + "/deploy-${oldDockerServer.version}"
            }
            return "."
        }

        fun getResolvedDockerFile(project: Project, server: Server): Path {
            val dockerComposeStream = {}::class.java.classLoader.getResourceAsStream(dockerServerRelativePath())
            val newPath = "deploy-${server.version}/server-docker-compose.yaml"
            val destinationPath = IntegrationServerUtil.getRelativePathInIntegrationServerDist(project, newPath)
            dockerComposeStream?.let {
                FileUtil.copyFile(it, destinationPath)
            }
            val resultComposeFilePath = DockerComposeUtil.getResolvedDockerPath(project, newPath)

            val serverTemplate = resultComposeFilePath.toFile()

            val forceUpgrade = isPreviousInstallationServerDefined(project) && !server.previousInstallation

            val configuredTemplate = serverTemplate.readText(Charsets.UTF_8)
                .replace("{{DEPLOY_SERVER_HTTP_PORT}}", server.httpPort.toString())
                .replace("{{DEPLOY_IMAGE_VERSION}}", getDockerImageVersion(server))
                .replace(
                    "{{DEPLOY_PLUGINS_TO_EXCLUDE}}",
                    server.defaultOfficialPluginsToExclude.joinToString(separator = ",")
                )
                .replace("{{DEPLOY_VERSION}}", server.version.toString())
                .replace("{{DEPLOY_FORCE_UPGRADE}}", forceUpgrade.toString())
                .replace("{{INTEGRATION_SERVER_ROOT_VOLUME}}", getOldDockerServerPath(project))

            serverTemplate.writeText(configuredTemplate)

            return resultComposeFilePath
        }

        private fun dockerServerRelativePath(): String {
            return "deploy/server-docker-compose.yaml"
        }

        fun runDockerBasedInstance(project: Project, server: Server) {
            project.exec {
                executable = "docker-compose"
                args = listOf("-f", getResolvedDockerFile(project, server).toFile().toString(), "up", "-d")
            }
        }

        fun stopDockerContainer(project: Project, server: Server) {
            project.logger.lifecycle("Trying to stop ${server.version} container")
            project.exec {
                executable = "docker-compose"
                args = arrayListOf("-f", getResolvedDockerFile(project, server).toFile().path, "stop")
            }
        }

        fun getDockerContainerPort(project: Project, server: Server, privatePort: Int): Int? {
            return ByteArrayOutputStream().use {
                project.exec {
                    executable = "docker-compose"
                    args = arrayListOf("-f", getResolvedDockerFile(project, server).toFile().toString(), "port", "deploy-${server.version}", privatePort.toString())
                    standardOutput = it
                    errorOutput = it
                    isIgnoreExitValue = true
                }
                val hostAndPort = it.toString(Charsets.UTF_8)
                if (hostAndPort.lowercase().contains("no container found")) {
                    null
                } else {
                    hostAndPort.split(':')[1].trim().toInt()
                }
            }
        }
    }
}
