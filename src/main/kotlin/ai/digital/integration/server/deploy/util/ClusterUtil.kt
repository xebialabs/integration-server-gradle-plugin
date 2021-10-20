package ai.digital.integration.server.deploy.util

import ai.digital.integration.server.common.domain.Cluster
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.util.DockerComposeUtil
import ai.digital.integration.server.common.util.IntegrationServerUtil
import ai.digital.integration.server.common.util.YamlFileUtil
import ai.digital.integration.server.deploy.tasks.cluster.ClusterConstants
import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Path

class ClusterUtil {

    companion object {

        private const val dockerXldHAPath = "deploy/cluster/docker-compose-xld-ha.yaml"
        private const val dockerXldHAWithWorkersPath = "deploy/cluster/docker-compose-xld-ha-slim-workers.yaml"

        private val workerToIp = mutableMapOf<Int, String>()
        private var lbIp: String? = null

        private fun getCluster(project: Project): Cluster {
            return DeployExtensionUtil.getExtension(project).cluster.get()
        }

        private fun getClusterVersion(project: Project): String? {
            val server = getServers(project).first()
            // Worker should be of the same version as server, otherwise it won't start.
            return server.version
        }

        fun isEnabled(project: Project): Boolean {
            return getCluster(project).enable.get()
        }

        private fun getServers(project: Project): List<Server> {
            return DeployExtensionUtil.getExtension(project).servers
                .filter { server -> !server.previousInstallation }
                .toList()
        }

        private fun getNumberOfServers(project: Project): Int {
            return getServers(project).size
        }

        private fun getServerVersionedImage(project: Project): String {
            val server = getServers(project).first()
            if (server.dockerImage.isNullOrBlank()) {
                throw RuntimeException("Incorrect configuration. Server should have `dockerImage` field defined.")
            }
            return "${server.dockerImage}:${getClusterVersion(project)}"
        }

        private fun getWorkerVersionedImage(project: Project): String {
            val worker = WorkerUtil.getWorkers(project).first()
            if (worker.dockerImage.isNullOrBlank()) {
                throw RuntimeException("Incorrect configuration. Worker should have `dockerImage` field defined.")
            }
            return "${worker.dockerImage}:${getClusterVersion(project)}"
        }

        private fun getResolvedXldHaDockerComposeFile(project: Project): Path {
            val serverTemplate = getTemplate(project, dockerXldHAPath)

            val configuredTemplate = serverTemplate.readText(Charsets.UTF_8)
                .replace("{{DEPLOY_MASTER_IMAGE}}", getServerVersionedImage(project))
                .replace("{{INTEGRATION_SERVER_ROOT_VOLUME}}", IntegrationServerUtil.getDist(project))
                .replace("{{DEPLOY_NETWORK_NAME}}", ClusterConstants.NETWORK_NAME)

            serverTemplate.writeText(configuredTemplate)
            return serverTemplate.toPath()
        }

        private fun getResolvedXldHaWithWorkersDockerComposeFile(project: Project): Path {
            val template = getTemplate(project, dockerXldHAWithWorkersPath)

            val configuredTemplate = template.readText(Charsets.UTF_8)
                .replace("{{DEPLOY_WORKER_IMAGE}}", getWorkerVersionedImage(project))
                .replace("{{INTEGRATION_SERVER_ROOT_VOLUME}}", IntegrationServerUtil.getDist(project))
                .replace("{{DEPLOY_NETWORK_NAME}}", ClusterConstants.NETWORK_NAME)

            template.writeText(configuredTemplate)
            overrideWorkerCommand(project, template)
            return template.toPath()
        }

        private fun overrideWorkerCommand(project: Project, template: File) {
            val commandArgs = mutableListOf("-api", "http://${lbIp}:5000/")

            for (orderNum in 1..this.getNumberOfServers(project)) {
                commandArgs.add("-master")
                commandArgs.add("${workerToIp[orderNum]}:8180")
            }
            val pairs = mutableMapOf<String, Any>("services.xl-deploy-worker.command" to commandArgs)
            YamlFileUtil.overlayFile(template, pairs)
        }

        private fun getTemplate(project: Project, path: String): File {
            val resultComposeFilePath = DockerComposeUtil.getResolvedDockerPath(project, path)
            return resultComposeFilePath.toFile()
        }

        private fun networkExists(project: Project): Boolean {
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

        fun createNetwork(project: Project) {
            if (!networkExists(project)) {
                project.exec {
                    it.executable = "docker"
                    it.args = listOf("network", "create", ClusterConstants.NETWORK_NAME)
                }
            }
        }

        fun runServers(project: Project) {
            val num = getNumberOfServers(project)
            val args = listOf(
                "-f",
                getResolvedXldHaDockerComposeFile(project).toFile().toString(),
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

        fun runWorkers(project: Project) {
            val num = WorkerUtil.getNumberOfWorkers(project).toString()
            val args = listOf(
                "-f",
                getResolvedXldHaWithWorkersDockerComposeFile(project).toFile().toString(),
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

        fun inspectIps(project: Project) {
            for (orderNum in 1..getNumberOfServers(project)) {
                workerToIp[orderNum] = getMasterIp(project, orderNum)
            }
            lbIp = getLbIp(project)
        }

        private fun getLbIp(project: Project): String {
            return DockerComposeUtil.inspect(project,
                "{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}",
                "xl-deploy-lb")
        }

        private fun getMasterIp(project: Project, order: Int): String {
            return DockerComposeUtil.inspect(project,
                "{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}",
                "cluster_xl-deploy-master_${order}")
        }
    }
}
