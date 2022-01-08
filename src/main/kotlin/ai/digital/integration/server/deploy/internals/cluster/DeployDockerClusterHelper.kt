package ai.digital.integration.server.deploy.internals.cluster

import ai.digital.integration.server.common.cluster.DockerClusterHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.domain.profiles.DockerComposeProfile
import ai.digital.integration.server.common.util.*
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.internals.EntryPointUrlUtil
import ai.digital.integration.server.deploy.internals.WorkerUtil
import ai.digital.integration.server.deploy.tasks.cluster.ClusterConstants
import net.jodah.failsafe.Failsafe
import net.jodah.failsafe.RetryPolicy
import org.gradle.api.Project
import org.gradle.process.internal.ExecException
import java.io.File
import java.nio.file.Path
import java.time.temporal.ChronoUnit
import java.util.*

open class DeployDockerClusterHelper(val project: Project) : DockerClusterHelper {

    companion object {
        private const val clusterMetadataPath = "deploy/cluster/cluster-metadata.properties"
        private const val dockerXldHAPath = "deploy/cluster/docker-compose-xld-ha.yaml"
        private const val dockerXldHAWithWorkersPath = "deploy/cluster/docker-compose-xld-ha-slim-workers.yaml"
        private const val rabbitMqEnabledPluginsPath = "deploy/cluster/rabbitmq/enabled_plugins"
        private const val privateDebugPort = 4005

        private val pluginsFolders = listOf("plugins", "plugins/__local__", "plugins/xld-official")

        private val serverMountedVolumes = listOf("centralConfiguration", "conf", *pluginsFolders.toTypedArray())
        private val workerMountedVolumes = listOf("conf", *pluginsFolders.toTypedArray())
    }

    private val workerToIp = mutableMapOf<Int, String>()
    private var lbIp: String? = null

    private fun getProfile(): DockerComposeProfile {
        return DeployExtensionUtil.getExtension(project).clusterProfiles.dockerCompose()
    }

    private fun getClusterVersion(): String? {
        val server = getServers().first()
        // Worker should be of the same version as server, otherwise it won't start.
        return server.version
    }

    private fun getServers(): List<Server> {
        return DeployExtensionUtil.getExtension(project).servers
            .filter { server -> !server.previousInstallation }
            .toList()
    }

    private fun getNumberOfServers(): Int {
        return getServers().size
    }

    private fun getServerVersionedImage(): String {
        val server = getServers().first()
        if (server.dockerImage.isNullOrBlank()) {
            throw RuntimeException("Incorrect configuration. Server should have `dockerImage` field defined.")
        }
        return "${server.dockerImage}:${getClusterVersion()}"
    }

    private fun getWorkerVersionedImage(): String {
        val worker = WorkerUtil.getWorkers(project).first()
        if (worker.dockerImage.isNullOrBlank()) {
            throw RuntimeException("Incorrect configuration. Worker should have `dockerImage` field defined.")
        }
        return "${worker.dockerImage}:${getClusterVersion()}"
    }

    private fun configureRabbitMq() {
        val dockerComposeStream = {}::class.java.classLoader.getResourceAsStream(rabbitMqEnabledPluginsPath)
        val resultComposeFilePath =
            IntegrationServerUtil.getRelativePathInIntegrationServerDist(project, "rabbitmq/enabled_plugins")
        resultComposeFilePath.parent.toFile().mkdirs()
        dockerComposeStream?.let {
            FileUtil.copyFile(it, resultComposeFilePath)
        }
    }

    override fun getClusterPublicPort(): String {
        return DeployServerUtil.getCluster(project).publicPort.toString()
    }

    private fun createClusterMetadata() {
        val path = IntegrationServerUtil.getRelativePathInIntegrationServerDist(project, clusterMetadataPath)
        val props = Properties()
        props["cluster.port"] = getClusterPublicPort()
        PropertiesUtil.writePropertiesFile(path.toFile(), props)
    }

    private fun getResolvedXldHaDockerComposeFile(): Path {
        val template = getTemplate(dockerXldHAPath)
        val serviceName = "xl-deploy-master"

        val configuredTemplate = template.readText(Charsets.UTF_8)
            .replace("{{DEPLOY_MASTER_IMAGE}}", getServerVersionedImage())
            .replace("{{DEPLOY_NETWORK_NAME}}", ClusterConstants.NETWORK_NAME)
            .replace("{{HA_PORT}}", HTTPUtil.findFreePort().toString())
            .replace("{{INTEGRATION_SERVER_ROOT_VOLUME}}", IntegrationServerUtil.getDist(project))
            .replace("{{DB_PORT}}", HTTPUtil.findFreePort().toString())
            .replace("{{POSGRES_COMMAND}}", getProfile().postgresCommand.get())
            .replace("{{POSTGRES_IMAGE}}", getProfile().postgresImage.get())
            .replace("{{PUBLIC_PORT}}", getClusterPublicPort())
            .replace("{{RABBIT_MQ_IMAGE}}", getProfile().rabbitMqImage.get())

        template.writeText(configuredTemplate)
        openDebugPort(template, serviceName, "4000-4049")

        return template.toPath()
    }

    private fun getResolvedXldHaWithWorkersDockerComposeFile(): Path {
        val template = getTemplate(dockerXldHAWithWorkersPath)
        val serviceName = "xl-deploy-worker"
        val configuredTemplate = template.readText(Charsets.UTF_8)
            .replace("{{DEPLOY_WORKER_IMAGE}}", getWorkerVersionedImage())
            .replace("{{INTEGRATION_SERVER_ROOT_VOLUME}}", IntegrationServerUtil.getDist(project))
            .replace("{{DEPLOY_NETWORK_NAME}}", ClusterConstants.NETWORK_NAME)

        template.writeText(configuredTemplate)
        overrideWorkerCommand(template)
        openDebugPort(template, serviceName, "4050-4100")

        return template.toPath()
    }

    private fun openDebugPort(template: File, serviceName: String, range: String) {

        fun getServiceOpts(): String {
            val suspend = if (DeployServerUtil.getCluster(project).debugSuspend) "y" else "n"
            return "DEPLOYIT_SERVER_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=${suspend},address=*:$privateDebugPort"
        }

        if (DeployServerUtil.getCluster(project).enableDebug) {
            val variables = getEnvironmentVariables(template, serviceName)
            variables.add(getServiceOpts())
            variables.sort()

            val pairs = mutableMapOf<String, Any>(
                "services.$serviceName.environment" to variables,
                "services.$serviceName.ports" to listOf("$range:$privateDebugPort")
            )
            YamlFileUtil.overlayFile(template, pairs)
            fixDockerComposeVersion(template)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getEnvironmentVariables(template: File, serviceName: String): MutableList<String> {
        return YamlFileUtil.readFileKey(template, "services.$serviceName.environment") as MutableList<String>
    }

    private fun overrideWorkerCommand(template: File) {
        val commandArgs = mutableListOf("-api", "http://${lbIp}:5000/")

        for (orderNum in 1..this.getNumberOfServers()) {
            commandArgs.add("-master")
            commandArgs.add("${workerToIp[orderNum]}:8180")
        }
        val pairs = mutableMapOf<String, Any>("services.xl-deploy-worker.command" to commandArgs)
        YamlFileUtil.overlayFile(template, pairs)
        fixDockerComposeVersion(template)
    }

    private fun fixDockerComposeVersion(template: File) {
        // fix for docker-compose version
        val fixedTemplate = template.readText(Charsets.UTF_8)
            .replace("version: 3.4", "version: \"3.4\"")

        template.writeText(fixedTemplate)
    }

    private fun getTemplate(path: String): File {
        val resultComposeFilePath = DockerComposeUtil.getResolvedDockerPath(project, path)
        return resultComposeFilePath.toFile()
    }

    private fun networkExists(): Boolean {
        return DockerUtil.execute(
            project,
            listOf("network",
                "ls",
                "--filter",
                "name=^${ClusterConstants.NETWORK_NAME}$",
                "--format=\"{{ .Name }}\"")
        ).isNotBlank()
    }

    private fun createNetwork() {
        if (!networkExists()) {
            project.exec {
                executable = "docker"
                args = listOf("network", "create", ClusterConstants.NETWORK_NAME)
            }
        }
    }

    private fun runServers() {
        configureRabbitMq()
        createServerFolders()

        val num = getNumberOfServers()
        val args = listOf(
            "-f",
            getResolvedXldHaDockerComposeFile().toFile().toString(),
            "up",
            "-d",
            "--scale",
            "xl-deploy-master=$num"
        )
        project.logger.lifecycle("Running $num server(s) with a command: `docker-compose ${
            args.joinToString(separator = " ")
        }`")

        DockerComposeUtil.execute(project, args)
    }

    private fun runWorkers() {
        createWorkerFolders()

        val num = WorkerUtil.getNumberOfWorkers(project).toString()
        val args = listOf(
            "-f",
            getResolvedXldHaWithWorkersDockerComposeFile().toFile().toString(),
            "up",
            "-d",
            "--scale",
            "xl-deploy-worker=$num"
        )
        project.logger.lifecycle("Running $num workers(s) with a command: `docker-compose ${
            args.joinToString(separator = " ")
        }`")

        DockerComposeUtil.execute(project, args)
    }

    private fun inspectIps() {
        for (orderNum in 1..getNumberOfServers()) {
            workerToIp[orderNum] = getMasterIp(orderNum)
        }
        lbIp = getLbIp()
    }

    private fun getLbIp(): String? {
        val maxAttempts = 5

        fun inspectLbIp(): String {
            return DockerUtil.inspect(project,
                "{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}",
                "xl-deploy-lb")
        }

        val retryPolicy = RetryPolicy<String>()
            .withMaxAttempts(maxAttempts)
            .withBackoff(1, 30, ChronoUnit.SECONDS)
            .handleResult("")
            .onRetriesExceeded { project.logger.warn("Failed to inspect Load Balancer IP. Max retries $maxAttempts exceeded.") }
            .onRetryScheduled { project.logger.lifecycle("Retry scheduled {}.") }

        return Failsafe.with(retryPolicy).get { -> inspectLbIp() }
    }

    private fun getMasterIp(order: Int): String {
        try {
            return DockerUtil.inspect(project,
                "{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}",
                "cluster_xl-deploy-master_${order}")
        } catch (e: ExecException) {
            // fallback in naming
            return DockerUtil.inspect(project,
                "{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}",
                "cluster-xl-deploy-master-${order}")
        }
    }

    fun shutdownCluster() {
        val args = listOf(
            "-f",
            getResolvedXldHaWithWorkersDockerComposeFile().toFile().toString(),
            "-f",
            getResolvedXldHaDockerComposeFile().toFile().toString(),
            "down"
        )
        DockerComposeUtil.execute(project, args)
    }

    private fun waitForBoot() {
        val url = EntryPointUrlUtil(project, ProductName.DEPLOY).composeUrl("/deployit/metadata/type")
        val server = DeployServerUtil.getServer(project)
        WaitForBootUtil.byPort(project, "Deploy", url, null, server.pingRetrySleepTime, server.pingTotalTries)
    }

    private fun createServerFolders() {
        serverMountedVolumes.forEach { folderName ->
            val folderPath = "${IntegrationServerUtil.getDist(project)}/xl-deploy-server/${folderName}"
            val folder = File(folderPath)
            folder.mkdirs()
            giveAllPermissionsForMountedVolume(folderPath)
            project.logger.lifecycle("Folder $folderPath has been created.")
        }
    }

    private fun createWorkerFolders() {
        workerMountedVolumes.forEach { folderName ->
            val folderPath = "${IntegrationServerUtil.getDist(project)}/xl-deploy-worker/${folderName}"
            val folder = File(folderPath)
            folder.mkdirs()
            giveAllPermissionsForMountedVolume(folderPath)
            project.logger.lifecycle("Folder $folderPath has been created.")
        }
    }

    private fun giveAllPermissionsForMountedVolume(folderPath: String) {
        ProcessUtil.chMod(
            project,
            "777",
            folderPath
        )
    }

    fun launchCluster() {
        createNetwork()
        runServers()
        inspectIps()
        runWorkers()
        createClusterMetadata()
        waitForBoot()
    }
}
