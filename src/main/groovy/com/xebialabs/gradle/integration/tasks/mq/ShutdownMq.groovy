package com.xebialabs.gradle.integration.tasks.mq

import com.xebialabs.gradle.integration.util.DockerComposeUtil
import com.xebialabs.gradle.integration.util.MqUtil
import com.xebialabs.gradle.integration.util.PluginUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

import java.nio.file.Path

class ShutdownMq extends DefaultTask {
    static NAME = "shutdownMq"
    def envPath = DockerComposeUtil.dockerfileDestination(project, "mq/mq.env")

    ShutdownMq() {
        this.group = PluginUtil.PLUGIN_GROUP
    }

    @InputFiles
    File getDockerComposeFile() {
        Path composeFile = DockerComposeUtil.dockerfileDestination(project, MqUtil.getMqFileName(project))
        return project.file(composeFile)
    }

    @TaskAction
    void stop() {
        project.logger.lifecycle("Stopping MQ.")
        project.exec {
            it.executable 'docker-compose'
            it.args '-f', getDockerComposeFile(), '--env-file', "${envPath}", 'down'
        }
    }
}
