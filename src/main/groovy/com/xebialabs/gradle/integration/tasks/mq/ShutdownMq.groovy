package com.xebialabs.gradle.integration.tasks.mq

import com.xebialabs.gradle.integration.constant.PluginConstant
import com.xebialabs.gradle.integration.util.MqUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

class ShutdownMq extends DefaultTask {
    static NAME = "shutdownMq"

    ShutdownMq() {
        this.group = PluginConstant.PLUGIN_GROUP
    }

    @InputFiles
    File getDockerComposeFile() {
        return MqUtil.getResolvedDockerFile(project).toFile()
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
