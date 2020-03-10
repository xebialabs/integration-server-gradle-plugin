package com.xebialabs.gradle.integration.tasks

import com.palantir.gradle.docker.DockerComposeUp
import com.xebialabs.gradle.integration.util.DockerComposeUtil
import com.xebialabs.gradle.integration.util.FileUtil
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

import java.nio.file.Path

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class StartRabbitMq extends DockerComposeUp {
    static NAME = "startRabbitMq"
    static COMPOSE_FILE_NAME = "rabbitmq-compose.yaml"

    StartRabbitMq() {
        this.configure {
            group = PLUGIN_GROUP
        }

    }

    @Override
    String getDescription() {
        return "Starts rabbit mq using `docker-compose` and ${COMPOSE_FILE_NAME} file."
    }

    @InputFiles
    File getDockerComposeFile() {
        InputStream dockerComposeStream = StartRabbitMq.class.classLoader
                .getResourceAsStream(COMPOSE_FILE_NAME)
        Path resultComposeFilePath = DockerComposeUtil.dockerComposeFileDestination(project, COMPOSE_FILE_NAME)
        FileUtil.copyFile(dockerComposeStream, resultComposeFilePath)

        return project.file(resultComposeFilePath)
    }

    @TaskAction
    void run() {
        project.logger.lifecycle("Starting Rabbit MQ.")
        super.run()
    }
}
