package ai.digital.integration.server.deploy.tasks.cluster

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.util.ClusterUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

open class DockerComposeBasedDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "dockerComposeBasedDeployCluster"
        const val NETWORK_NAME = "xld-network"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        createNetwork()
        runServers()
        inspectIps()

//        project.exec {
//            it.executable = "docker-compose"
//            it.args = listOf("-f",
//                ClusterUtil.getResolvedXldHaWithWorkersDockerComposeFile(project).toFile().toString(),
//                "up",
//                "-d")
//        }
    }

    private fun networkExists(): Boolean {
        val stdout = ByteArrayOutputStream()
        project.exec {
            it.executable = "docker"
            it.args = listOf("network", "ls", "--filter", "name=^${NETWORK_NAME}$", "--format=\"{{ .Name }}\"")
            it.standardOutput = stdout
        }
        return stdout.toString(StandardCharsets.UTF_8).toBoolean()
    }

    private fun createNetwork() {
        if (!networkExists()) {
            project.exec {
                it.executable = "docker"
                it.args = listOf("network", "create", NETWORK_NAME, "||", "true")
            }
        }
    }

    private fun runServers() {
        val num = ClusterUtil.getNumberOfServers(project).toString()
        val args = listOf(
            "-f",
            ClusterUtil.getResolvedXldHaDockerComposeFile(project).toFile().toString(),
            "up",
            "-d",
            "--scale",
            num
        )
        project.logger.lifecycle("Running $num server(s) with command: `docker-compose ${args.joinToString(separator = " ")}}`")
        project.exec {
            it.executable = "docker-compose"
            it.args = args
        }
    }

    private fun inspectIps() {
        getMasterIp(1)
        getMasterIp(2)
    }

    private fun getMasterIp(order: Int): String {
        val stdout = ByteArrayOutputStream()
        project.exec {
            it.executable = "docker"
            it.args = listOf(
                "inspect",
                "-f",
                "'{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}'",
                "xl-deploy-ha_xl-deploy-master_${order}"
            )
            it.standardOutput = stdout
        }

        return stdout.toString(StandardCharsets.UTF_8)
    }
}
