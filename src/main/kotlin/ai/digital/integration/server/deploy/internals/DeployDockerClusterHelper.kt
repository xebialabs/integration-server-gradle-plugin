package ai.digital.integration.server.deploy.internals

import ai.digital.integration.server.common.domain.Cluster
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.util.*
import ai.digital.integration.server.deploy.tasks.cluster.ClusterConstants
import net.jodah.failsafe.Failsafe
import net.jodah.failsafe.RetryPolicy
import org.gradle.api.Project
import java.io.File
import java.nio.file.Path
import java.time.temporal.ChronoUnit
import java.util.*

open class DeployDockerClusterHelper(val project: Project) {

    companion object {
        private const val clusterMetadataPath = "deploy/cluster/cluster-metadata.properties"
        private const val dockerXldHAPath = "deploy/cluster/docker-compose-xld-ha.yaml"
        private const val dockerXldHAWithWorkersPath = "deploy/cluster/docker-compose-xld-ha-slim-workers.yaml"
        private const val rabbitMqEnabledPluginsPath = "deploy/cluster/rabbitmq/enabled_plugins"
        private const val privateDebugPort = 4005

        private val serverMountedVolumes =
            listOf("centralConfiguration", "conf", "plugins/__local", "plugins/xld-official")
        private val workerMountedVolumes = listOf("conf", "plugins/__local", "plugins/xld-official")
    }

    private val workerToIp = mutableMapOf<Int, String>()
    private var lbIp: String? = null

    private fun getCluster(): Cluster {
        return DeployExtensionUtil.getExtension(project).cluster.get()
    }

    private fun getClusterVersion(): String? {
        val server = getServers().first()
        // Worker should be of the same version as server, otherwise it won't start.
        return server.version
    }

    fun isClusterEnabled(): Boolean {
        return getCluster().enable
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

    fun getClusterPublicPort(): String {
        return getCluster().publicPort.toString()
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
            .replace("{{INTEGRATION_SERVER_ROOT_VOLUME}}", IntegrationServerUtil.getDist(project))
            .replace("{{DEPLOY_NETWORK_NAME}}", ClusterConstants.NETWORK_NAME)
            .replace("{{PUBLIC_PORT}}", getClusterPublicPort())
            .replace("{{HA_PORT}}", HTTPUtil.findFreePort().toString())
            .replace("{{DB_PORT}}", HTTPUtil.findFreePort().toString())

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
            val suspend = if (getCluster().debugSuspend) "y" else "n"
            return "DEPLOYIT_SERVER_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=${suspend},address=*:$privateDebugPort"
        }

        if (getCluster().enableDebug) {
            val variables = getEnvironmentVariables(template, serviceName)
            variables.add(getServiceOpts())
            variables.sort()

            val pairs = mutableMapOf<String, Any>(
                "services.$serviceName.environment" to variables,
                "services.$serviceName.ports" to listOf("$range:$privateDebugPort")
            )
            YamlFileUtil.overlayFile(template, pairs)
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
                it.executable = "docker"
                it.args = listOf("network", "create", ClusterConstants.NETWORK_NAME)
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
        return DockerUtil.inspect(project,
            "{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}",
            "cluster_xl-deploy-master_${order}")
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
        val url = EntryPointUrlUtil.composeUrl(project, "/deployit/metadata/type")
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
