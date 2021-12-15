package ai.digital.integration.server.common.mq

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.util.MqUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File

open class ShutdownMqTask : DefaultTask() {

    companion object {
        const val NAME = "shutdownMq"
    }

    init {
        this.group = PluginConstant.PLUGIN_GROUP
    }

    @InputFiles
    fun getDockerComposeFile(): File {
        return MqUtil.getResolvedDockerFile(project).toFile()
    }

    @TaskAction
    fun stop() {
        project.logger.lifecycle("Shutting down ${MqUtil.mqName(project)} MQ.")

        project.exec {
            executable = "docker-compose"
            args = arrayListOf("-f",
                getDockerComposeFile().path,
                "--project-directory",
                MqUtil.getMqDirectory(project),
                "down")
        }
    }
}
