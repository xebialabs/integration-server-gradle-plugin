package com.xebialabs.gradle.integration.tasks

import com.palantir.gradle.docker.DockerComposeUp
import com.xebialabs.gradle.integration.util.DbUtil
import org.apache.commons.io.IOUtils
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.util.PluginUtil.DIST_DESTINATION_NAME

class DockerComposeDatabaseStartTask extends DockerComposeUp {
    static NAME = "dockerComposeDatabaseStart"

    @InputFiles
    File getDockerComposeFile() {
        DbUtil.assertNotDerby(project, 'Docker compose tasks do not support Derby database.')

        def dbName = DbUtil.databaseName(project)
        def composeFile = "docker-compose_${dbName}.yaml"
        def dockerComposeStream = DockerComposeDatabaseStartTask.class.classLoader
            .getResourceAsStream("database-compose/${composeFile}")

        def resultComposeFilePath =
            "${project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString()}/${composeFile}"
        def resultComposeFile = new File(resultComposeFilePath)
        if (!resultComposeFile.getParentFile().exists()) {
            resultComposeFile.getParentFile().mkdirs()
        }
        resultComposeFile.createNewFile()
        def os = new FileOutputStream(resultComposeFilePath)
        IOUtils.copy(dockerComposeStream, os)
        return project.file(resultComposeFilePath)
    }

    @TaskAction
    void run() {
        super.run()
    }

}
