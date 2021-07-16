package com.xebialabs.gradle.integration.tasks.mq

import com.xebialabs.gradle.integration.util.DockerComposeUtil
import com.xebialabs.gradle.integration.util.PluginUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

import java.nio.file.Path

class ShutdownRabbitMq extends  DefaultTask{
    static NAME = "shutdownRabbitMq"

    ShutdownRabbitMq() {
        this.group = PluginUtil.PLUGIN_GROUP
    }

    @InputFiles
    File getDockerComposeFile() {
        Path composeFile  = DockerComposeUtil.dockerfileDestination(project, StartRabbitMq.COMPOSE_FILE_NAME)

        return project.file(composeFile)
    }

    @TaskAction
    void stop() {
        project.logger.lifecycle("Stopping Rabbit MQ.")
        project.exec {
            it.executable 'docker-compose'
            it.args '-f', getDockerComposeFile(), 'down'
        }
    }
}
