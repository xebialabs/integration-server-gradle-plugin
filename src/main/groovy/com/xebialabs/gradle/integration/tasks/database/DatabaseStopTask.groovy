package com.xebialabs.gradle.integration.tasks.database

import com.xebialabs.gradle.integration.util.DbUtil
import com.xebialabs.gradle.integration.util.DockerComposeUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.constant.PluginConstant.PLUGIN_GROUP

class DatabaseStopTask extends DefaultTask {
    static NAME = 'databaseStop'

    DatabaseStopTask() {
        this.group = PLUGIN_GROUP
    }

    @InputFiles
    File getDockerComposeFile() {
        DbUtil.assertNotDerby(project, 'Docker compose tasks do not support Derby database.')

        def composeFilePath = DbUtil.dockerComposeFileDestination(project)
        return composeFilePath.toFile()
    }

    @TaskAction
    void run() {
        project.exec {
            it.executable 'docker-compose'
            it.args '-f', getDockerComposeFile(), 'down'
        }
    }
}
