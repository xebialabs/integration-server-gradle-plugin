package com.xebialabs.gradle.integration.tasks.mq

import com.palantir.gradle.docker.DockerComposeUp
import com.xebialabs.gradle.integration.util.DockerComposeUtil
import com.xebialabs.gradle.integration.util.FileUtil
import com.xebialabs.gradle.integration.util.MqUtil
import com.xebialabs.gradle.integration.util.PropertyUtil
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

import java.nio.file.Path

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class StartMq extends DockerComposeUp {
    static NAME = "startMq"

    StartMq() {
        this.configure {
            group = PLUGIN_GROUP
        }

    }

    @Override
    String getDescription() {
        return "Starts rabbit mq using `docker-compose` and ${MqUtil.getMqFileName(project)} file."
    }

    @InputFiles
    File getDockerComposeFile() {
        InputStream dockerComposeStream = StartMq.class.classLoader.getResourceAsStream(MqUtil.getMqFileName(project))

        Path resultComposeFilePath = DockerComposeUtil.dockerfileDestination(project, MqUtil.getMqFileName(project))
        FileUtil.copyFile(dockerComposeStream, resultComposeFilePath)

        def mqTemplate = resultComposeFilePath.toFile()
        def mqPort = PropertyUtil.resolveIntValue(project, "mqPort", MqUtil.mqName(project) == MqUtil.RABBITMQ ? 5672 : 61616)

        def configuredTemplate = mqTemplate.text
                .replace('RABBITMQ_PORT2', "${mqPort}:5672")
                .replace('ACTIVEMQ_PORT2', "${mqPort}:61616")
        mqTemplate.text = configuredTemplate

        return project.file(resultComposeFilePath)
    }

    @TaskAction
    void run() {
        project.logger.lifecycle("Starting ${MqUtil.mqName(project)} MQ.")
        project.exec {
            it.executable "docker-compose"
            it.args '-f', getDockerComposeFile(), '--project-directory', "${MqUtil.getProjectDirectory(project)}/mq", 'up', '-d'
        }

    }
}
