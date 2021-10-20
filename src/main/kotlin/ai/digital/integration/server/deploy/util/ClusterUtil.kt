package ai.digital.integration.server.deploy.util

import ai.digital.integration.server.common.domain.Cluster
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.util.DockerComposeUtil
import ai.digital.integration.server.common.util.IntegrationServerUtil
import org.gradle.api.Project
import java.io.File
import java.nio.file.Path

class ClusterUtil {

    companion object {

        private const val dockerXldHAPath = "deploy/cluster/docker-compose-xld-ha.yaml"
        private const val dockerXldHAWithWorkersPath = "deploy/cluster/docker-compose-xld-ha-slim-workers.yaml"

        private fun getCluster(project: Project): Cluster {
            return DeployExtensionUtil.getExtension(project).cluster.get()
        }

        fun isEnabled(project: Project): Boolean {
            return getCluster(project).enable.get()
        }

        private fun getServers(project: Project): List<Server> {
            return DeployExtensionUtil.getExtension(project).servers
                .filter { server -> !server.previousInstallation }
                .toList()
        }

        fun getNumberOfServers(project: Project): Int {
            return getServers(project).size
        }

        private fun getServerVersionedImage(project: Project): String {
            val server = getServers(project).first()
            if (server.dockerImage.isNullOrBlank()) {
                throw RuntimeException("Incorrect configuration. Server should have `dockerImage` field defined.")
            }
            return "${server.dockerImage}:${server.version}"
        }

        private fun getWorkerVersionedImage(project: Project): String {
            val worker = WorkerUtil.getWorkers(project).first()
            if (worker.dockerImage.isNullOrBlank()) {
                throw RuntimeException("Incorrect configuration. Worker should have `dockerImage` field defined.")
            }
            return "${worker.dockerImage}:${worker.version}"
        }

        fun getResolvedXldHaDockerComposeFile(project: Project): Path {
            val serverTemplate = getTemplate(project, dockerXldHAPath)

            val configuredTemplate = serverTemplate.readText(Charsets.UTF_8)
                .replace("{{DEPLOY_MASTER_IMAGE}}", getServerVersionedImage(project))
                .replace("{{INTEGRATION_SERVER_ROOT_VOLUME}}", IntegrationServerUtil.getDist(project))

            serverTemplate.writeText(configuredTemplate)
            return serverTemplate.toPath()
        }

        fun getResolvedXldHaWithWorkersDockerComposeFile(project: Project): Path {
            val serverTemplate = getTemplate(project, dockerXldHAWithWorkersPath)

            val configuredTemplate = serverTemplate.readText(Charsets.UTF_8)
                .replace("{{DEPLOY_WORKER_IMAGE}}", getWorkerVersionedImage(project))
                .replace("{{INTEGRATION_SERVER_ROOT_VOLUME}}", IntegrationServerUtil.getDist(project))

            serverTemplate.writeText(configuredTemplate)
            return serverTemplate.toPath()
        }

//        private fun createWorkerCommand(): String { TODO:
//            return """
//                - "-api"
//              - "http://${XLD_LB}:5000/"
//              - "-master"
//              - "${XLD_MASTER_1}:8180"
//              - "-master"
//              - "${XLD_MASTER_2}:8180"
//            """.trimIndent()
//        }

        private fun getTemplate(project: Project, path: String): File {
            val resultComposeFilePath = DockerComposeUtil.getResolvedDockerPath(project, path)
            return resultComposeFilePath.toFile()
        }
    }
}
