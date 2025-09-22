package ai.digital.integration.server.common.mq

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.MqUtil
import ai.digital.integration.server.deploy.internals.WorkerUtil
import ai.digital.integration.server.deploy.tasks.centralConfiguration.DownloadAndExtractCentralConfigurationServerDistTask
import ai.digital.integration.server.deploy.tasks.cli.DownloadAndExtractCliDistTask
import ai.digital.integration.server.deploy.tasks.server.DownloadAndExtractServerDistTask
import com.palantir.gradle.docker.DockerComposeUp
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

abstract class StartMqTask @Inject constructor(
    private val execOperations: ExecOperations) : DockerComposeUp() {

    companion object {
        const val NAME = "startMq"
    }

    init {
        this.group = PLUGIN_GROUP
        this.onlyIf {
            WorkerUtil.hasWorkers(project)
        }
        this.mustRunAfter(DownloadAndExtractServerDistTask.NAME, DownloadAndExtractCliDistTask.NAME, DownloadAndExtractCentralConfigurationServerDistTask.NAME)
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
        project.logger.lifecycle("Cleaning up previous Mq containers and networks.")
        execOperations.exec {
            executable = "docker-compose"
            args = arrayListOf("-f",
                getDockerComposeFile().path,
                "--project-directory",
                MqUtil.getMqDirectory(project),
                "down")
        }
        project.logger.lifecycle("Starting ${MqUtil.mqName(project)} MQ.")
        execOperations.exec {
            executable = "docker-compose"
            args = arrayListOf("-f",
                dockerComposeFile.path,
                "--project-directory",
                MqUtil.getMqDirectory(project),
                "up",
                "-d")
        }

    }
}
