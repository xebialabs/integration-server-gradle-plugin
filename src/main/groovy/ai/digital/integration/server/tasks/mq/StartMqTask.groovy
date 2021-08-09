package ai.digital.integration.server.tasks.mq

import ai.digital.integration.server.util.MqUtil
import ai.digital.integration.server.util.WorkerUtil
import com.palantir.gradle.docker.DockerComposeUp
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class StartMqTask extends DockerComposeUp {
    static NAME = "startMq"

    StartMqTask() {
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
            it.args '-f', getDockerComposeFile(), '--project-directory', MqUtil.getMqDirectory(project), 'up', '-d'
        }

    }
}
