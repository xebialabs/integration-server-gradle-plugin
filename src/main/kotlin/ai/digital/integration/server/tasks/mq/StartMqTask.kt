package ai.digital.integration.server.tasks.mq

import ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.util.MqUtil
import ai.digital.integration.server.util.WorkerUtil
import com.palantir.gradle.docker.DockerComposeUp
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class StartMqTask : DockerComposeUp() {

    companion object {
        @JvmStatic
        val NAME = "startMq"
    }

    init {
        this.group = PLUGIN_GROUP
        this.onlyIf {
            WorkerUtil.hasWorkers(project)
        }
    }

    override fun getDescription(): String {
        return "Starts RabbitMQ using `docker-compose` and ${MqUtil.getMqRelativePath(project)} file."
    }

    @InputFiles
    override fun getDockerComposeFile(): File {
        return project.file(MqUtil.getResolvedDockerFile(project))
    }

    @TaskAction
    override fun run() {
        project.logger.lifecycle("Starting ${MqUtil.mqName(project)} MQ.")

        project.exec {
            it.executable = "docker-compose"
            it.args = arrayListOf("-f",
                dockerComposeFile.path,
                "--project-directory",
                MqUtil.getMqDirectory(project),
                "up",
                "-d")
        }

    }
}