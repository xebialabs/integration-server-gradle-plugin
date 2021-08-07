package com.xebialabs.gradle.integration.tasks.mq

import com.palantir.gradle.docker.DockerComposeUp
import com.xebialabs.gradle.integration.util.MqUtil
import com.xebialabs.gradle.integration.util.WorkerUtil
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.constant.PluginConstant.PLUGIN_GROUP

class StartMq extends DockerComposeUp {
    static NAME = "startMq"

    StartMq() {
        this.configure {
            group = PLUGIN_GROUP
            onlyIf {
                WorkerUtil.hasWorkers(project)
            }
        }
    }

    @Override
    String getDescription() {
        "Starts rabbit mq using `docker-compose` and ${MqUtil.getMqRelativePath(project)} file."
    }

    @InputFiles
    File getDockerComposeFile() {
        project.file(MqUtil.getResolvedDockerFile(project))
    }

    @TaskAction
    void run() {
        project.logger.lifecycle("Starting ${MqUtil.mqName(project)} MQ.")

        project.exec {
            it.executable "docker-compose"
            it.args '-f', getDockerComposeFile(), '--project-directory', "${MqUtil.getMqDirectory(project)}/mq", 'up', '-d'
        }

    }
}
