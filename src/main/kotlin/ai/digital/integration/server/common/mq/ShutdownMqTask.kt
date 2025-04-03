package ai.digital.integration.server.common.mq

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.util.MqUtil
import ai.digital.integration.server.deploy.tasks.centralConfiguration.DownloadAndExtractCentralConfigurationServerDistTask
import ai.digital.integration.server.deploy.tasks.cli.DownloadAndExtractCliDistTask
import ai.digital.integration.server.deploy.tasks.server.DownloadAndExtractServerDistTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

open class ShutdownMqTask @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {

    companion object {
        const val NAME = "shutdownMq"
    }

    init {
        this.group = PluginConstant.PLUGIN_GROUP
        this.mustRunAfter(DownloadAndExtractServerDistTask.NAME, DownloadAndExtractCliDistTask.NAME, DownloadAndExtractCentralConfigurationServerDistTask.NAME)
    }

    @InputFiles
    fun getDockerComposeFile(): File {
        return MqUtil.getResolvedDockerFile(project).toFile()
    }

    @TaskAction
    fun stop() {
        project.logger.lifecycle("Shutting down ${MqUtil.mqName(project)} MQ.")

        execOperations.exec {
            executable("docker-compose")
            args(arrayListOf("-f",
                getDockerComposeFile().path,
                "--project-directory",
                MqUtil.getMqDirectory(project),
                "down"))
        }
    }
}
