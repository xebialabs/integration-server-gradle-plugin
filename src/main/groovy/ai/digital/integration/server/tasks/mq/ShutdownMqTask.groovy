package ai.digital.integration.server.tasks.mq

import ai.digital.integration.server.util.MqUtil
import ai.digital.integration.server.constant.PluginConstant
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

class ShutdownMqTask extends DefaultTask {
    public static String NAME = "shutdownMq"

    ShutdownMqTask() {
        this.group = PluginConstant.PLUGIN_GROUP
    }

    @InputFiles
    File getDockerComposeFile() {
        MqUtil.getResolvedDockerFile(project).toFile()
    }

    @TaskAction
    void stop() {
        project.logger.lifecycle("Shutting down ${MqUtil.mqName(project)} MQ.")

        project.exec {
            it.executable 'docker-compose'
            it.args '-f', getDockerComposeFile(), '--project-directory', MqUtil.getMqDirectory(project), 'down'
        }
    }
}
