package ai.digital.integration.server.deploy.tasks.server.operator

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.util.DbUtil
import ai.digital.integration.server.common.util.HTTPUtil
import ai.digital.integration.server.common.util.YamlFileUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class OperatorCentralConfigurationTask : DefaultTask() {

    companion object {
        const val NAME = "operatorCentralConfiguration"
    }

    init {
        this.group = PLUGIN_GROUP
    }

    private fun overlayRepositoryConfig(serverDir: String) {
        project.logger.lifecycle("Creating a custom deploy-repository.yaml")

        val deployRepositoryYaml = File("${serverDir}/centralConfiguration/deploy-repository.yaml")

        DbUtil.dbConfig(project)?.let { config ->
            YamlFileUtil.writeFileValue(deployRepositoryYaml, config)
        }

        val configuredTemplate = deployRepositoryYaml.readText(Charsets.UTF_8)
            .replace("{{DB_PORT}}", DbUtil.getPort(project).toString())
        deployRepositoryYaml.writeText(configuredTemplate)
    }

    private fun createCentralConfigurationFiles(server: Server) {
        project.logger.lifecycle("Generating initial central configuration files for server ${server.name}")
        val serverDir = DeployServerUtil.getServerWorkingDir(project, server)

        overlayRepositoryConfig(serverDir)

        project.logger.lifecycle("Creating custom deploy-server.yaml")

        YamlFileUtil.overlayFile(
            File("${serverDir}/centralConfiguration/deploy-server.yaml"),
            mutableMapOf(
                "deploy.server.port" to HTTPUtil.findFreePort(),
                "deploy.server.hostname" to "127.0.0.1"
            )
        )

        project.logger.lifecycle("Creating custom deploy-task.yaml")
        YamlFileUtil.overlayFile(
            File("${serverDir}/centralConfiguration/deploy-task.yaml"),
            mutableMapOf(
                "deploy.task.in-process-worker" to "true",
                "deploy.task.planner.registries.timeout" to "5 minutes",
                "deploy.task.queue.name" to "xld-tasks-queue",
                "deploy.task.queue.archive-queue-name" to "xld-archive-queue")
        )
    }

    @TaskAction
    fun launch() {
        DeployServerUtil.getServers(project)
            .forEach { server ->
                if (server.numericVersion() >= 10.2) {
                    createCentralConfigurationFiles(server)
                }
            }
    }
}
