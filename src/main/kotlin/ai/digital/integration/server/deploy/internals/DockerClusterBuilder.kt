package ai.digital.integration.server.deploy.internals

import ai.digital.integration.server.common.domain.Cluster
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.util.DockerComposeUtil
import ai.digital.integration.server.common.util.FileUtil
import ai.digital.integration.server.common.util.IntegrationServerUtil
import ai.digital.integration.server.common.util.YamlFileUtil
import ai.digital.integration.server.deploy.tasks.cluster.ClusterConstants
import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Path

open class DockerClusterBuilder(val project: Project) {

    companion object {
        private const val dockerXldHAPath = "deploy/cluster/docker-compose-xld-ha.yaml"
        private const val dockerXldHAWithWorkersPath = "deploy/cluster/docker-compose-xld-ha-slim-workers.yaml"
        private const val rabbitMqEnabledPluginsPath = "deploy/cluster/rabbitmq/enabled_plugins"
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
        return getCluster().enable.get()
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

    private fun getPublicPort(): String {
        return getCluster().publicPort.get().toString()
    }

    private fun getResolvedXldHaDockerComposeFile(): Path {
        val serverTemplate = getTemplate(dockerXldHAPath)

        val configuredTemplate = serverTemplate.readText(Charsets.UTF_8)
            .replace("{{DEPLOY_MASTER_IMAGE}}", getServerVersionedImage())
            .replace("{{INTEGRATION_SERVER_ROOT_VOLUME}}", IntegrationServerUtil.getDist(project))
            .replace("{{DEPLOY_NETWORK_NAME}}", ClusterConstants.NETWORK_NAME)
            .replace("{{PUBLIC_PORT}}", getPublicPort())

        serverTemplate.writeText(configuredTemplate)
        return serverTemplate.toPath()
    }

    private fun getResolvedXldHaWithWorkersDockerComposeFile(): Path {
        val template = getTemplate(dockerXldHAWithWorkersPath)

        val configuredTemplate = template.readText(Charsets.UTF_8)
            .replace("{{DEPLOY_WORKER_IMAGE}}", getWorkerVersionedImage())
            .replace("{{INTEGRATION_SERVER_ROOT_VOLUME}}", IntegrationServerUtil.getDist(project))
            .replace("{{DEPLOY_NETWORK_NAME}}", ClusterConstants.NETWORK_NAME)

        template.writeText(configuredTemplate)
        overrideWorkerCommand(template)
        return template.toPath()
    }

    private fun overrideWorkerCommand(template: File) {
        val commandArgs = mutableListOf("-api", "http://${lbIp}:${getPublicPort()}/")

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
        val stdout = ByteArrayOutputStream()
        project.exec {
            it.executable = "docker"
            it.args = listOf("network",
                "ls",
                "--filter",
                "name=^${ClusterConstants.NETWORK_NAME}$",
                "--format=\"{{ .Name }}\"")
            it.standardOutput = stdout
        }
        return stdout.toString(StandardCharsets.UTF_8).isNotBlank()
    }

    fun createNetwork() {
        if (!networkExists()) {
            project.exec {
                it.executable = "docker"
                it.args = listOf("network", "create", ClusterConstants.NETWORK_NAME)
            }
        }
    }

    fun runServers() {
        configureRabbitMq()

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
        project.exec {
            it.executable = "docker-compose"
            it.args = args
        }
    }

    fun runWorkers() {
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
        project.exec {
            it.executable = "docker-compose"
            it.args = args
        }
    }

    private fun inspectIps() {
        for (orderNum in 1..getNumberOfServers()) {
            workerToIp[orderNum] = getMasterIp(orderNum)
        }
        lbIp = getLbIp()
    }

    private fun getLbIp(): String {
        return DockerComposeUtil.inspect(project,
            "{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}",
            "xl-deploy-lb")
    }

    private fun getMasterIp(order: Int): String {
        return DockerComposeUtil.inspect(project,
            "{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}",
            "cluster_xl-deploy-master_${order}")
    }

    fun buildCluster() {
        createNetwork()
        runServers()
        inspectIps()
        runWorkers()
    }
}
