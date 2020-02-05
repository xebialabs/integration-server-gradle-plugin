package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.util.DbUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

import static com.xebialabs.gradle.integration.util.PluginUtil.DIST_DESTINATION_NAME
import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class DockerComposeDatabaseStopTask extends DefaultTask {
    static NAME = 'dockerComposeDatabaseStop'

    DockerComposeDatabaseStopTask() {
        this.group = PLUGIN_GROUP
    }

    @InputFiles
    File getDockerComposeFile() {
        DbUtil.assertNotDerby(project, 'Docker compose tasks do not support Derby database.')

        def dbName = DbUtil.databaseName(project)
        def composeFile = "docker-compose_${dbName}.yaml"
        def composeFilePath =
            Paths.get("${project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString()}/${composeFile}")
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
