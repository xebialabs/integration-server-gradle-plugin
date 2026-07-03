package ai.digital.integration.server.common.tasks.infrastructure

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.domain.Infrastructure
import ai.digital.integration.server.common.util.InfrastructureUtil
import ai.digital.integration.server.deploy.internals.WorkerUtil
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class InfrastructureStartTask @Inject constructor(
    private val execOperations: ExecOperations) : DefaultTask() {
    companion object {
        const val NAME = "infrastructureStart"
    }

    init {
        this.group = PluginConstant.PLUGIN_GROUP
        this.onlyIf {
            WorkerUtil.hasWorkers(project)
        }
    }

    override fun getDescription(): String {
        return "Starts infrastructures."
    }

    @TaskAction
    fun run() {
        project.logger.lifecycle("Starting infrastructures.")

        InfrastructureUtil.getInfrastructures(project)
                .forEach { infrastructure ->
                    if (infrastructure.isDockerBased()) {
                        startDockerContainers(infrastructure)
                    }
                    // else if - add other infrastructure types (overcast, k8s, etc.)
                }
    }

    private fun startDockerContainers(infrastructure: Infrastructure) {
        project.logger.lifecycle("Starting infrastructure ${infrastructure.name} using `docker-compose`")

        val dockerComposeArgs = arrayListOf<String>()
        infrastructure.dockerComposeFiles.forEach { dockerComposeFile ->
            dockerComposeArgs.add("-f")
            dockerComposeArgs.add(dockerComposeFile)
        }
        dockerComposeArgs.add("up")
        dockerComposeArgs.add("-d")

        // Use 'docker compose' on Windows, 'docker-compose' on other systems
        val executable = if (Os.isFamily(Os.FAMILY_WINDOWS)) "docker" else "docker-compose"
        val baseArgs = if (Os.isFamily(Os.FAMILY_WINDOWS)) listOf("compose") else emptyList()

        execOperations.exec {
            this.executable = executable
            args = baseArgs + dockerComposeArgs
        }
    }
}