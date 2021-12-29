package ai.digital.integration.server.common.tasks.infrastructure

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.util.InfrastructureUtil
import ai.digital.integration.server.deploy.internals.WorkerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class InfrastructureStopTask : DefaultTask() {
    companion object {
        const val NAME = "infrastructureStop"
    }

    init {
        this.group = PluginConstant.PLUGIN_GROUP
        this.onlyIf {
            WorkerUtil.hasWorkers(project)
        }
    }

    override fun getDescription(): String {
        return "Stops infrastructures."
    }

    @TaskAction
    fun run() {
        project.logger.lifecycle("Starting infrastructures.")

        InfrastructureUtil.getInfrastructures(project)
                .forEach { infrastructure ->
                    if (infrastructure.isDockerBased()) {
                        project.logger.lifecycle("Stopping infrastructure ${infrastructure.name} using `docker-compose`")

                        val dockerComposeArgs = arrayListOf<String>()
                        infrastructure.dockerComposeFiles.forEach { dockerComposeFile ->
                            dockerComposeArgs.add("-f")
                            dockerComposeArgs.add(dockerComposeFile)
                        }
                        dockerComposeArgs.add("down")

                        project.exec {
                            executable = "docker-compose"
                            args = dockerComposeArgs
                        }
                    }
                    // else if - add other infrastructure types (overcast, k8s, etc.)
                }
    }
}