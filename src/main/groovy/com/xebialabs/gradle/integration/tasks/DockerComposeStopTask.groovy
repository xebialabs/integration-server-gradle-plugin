package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.util.DbUtil
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.util.PluginUtil.DIST_DESTINATION_NAME

class DockerComposeStopTask extends DefaultTask {
    static NAME = "dockerComposeStop"

    DockerComposeStopTask() {
        this.group = 'Docker'
    }

    @InputFiles
    File getDockerComposeFile() {
        def dbName = DbUtil.databaseName(project)

        if (DbUtil.isDerby(dbName)) {
            throw new GradleException('Docker compose tasks do not support Derby database.')
        }

        def composeFile = "docker-compose_${dbName}.yaml"
        def composeFilePath = "${project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString()}/${composeFile}"
        return project.file(composeFilePath)
    }

    @TaskAction
    void run() {
        project.exec {
            it.executable "docker-compose"
            it.args "-f", getDockerComposeFile(), "down"
        }
    }
}
