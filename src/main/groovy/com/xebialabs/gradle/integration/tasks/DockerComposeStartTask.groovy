package com.xebialabs.gradle.integration.tasks

import com.palantir.gradle.docker.DockerComposeUp
import com.xebialabs.gradle.integration.util.DbUtil
import org.apache.commons.io.IOUtils
import org.gradle.api.GradleException
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.util.PluginUtil.DIST_DESTINATION_NAME

class DockerComposeStartTask extends DockerComposeUp {
    static NAME = "dockerComposeStart"

    @InputFiles
    File getDockerComposeFile() {
        def dbName = DbUtil.databaseName(project)

        if (DbUtil.isDerby(dbName)) {
            throw new GradleException('Docker compose tasks do not support Derby database.')
        }

        def composeFile = "docker-compose_${dbName}.yaml"
        def dockerComposeStream = DockerComposeStartTask.class.classLoader.getResourceAsStream("database-compose/${composeFile}")
        def resultComposeFilePath = "${project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString()}/${composeFile}"
        new File(resultComposeFilePath).createNewFile()
        def os = new FileOutputStream(resultComposeFilePath)
        IOUtils.copy(dockerComposeStream, os)
        return project.file(resultComposeFilePath)
    }

    @TaskAction
    void run() {
        super.run()
    }

}
