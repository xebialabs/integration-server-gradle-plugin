package com.xebialabs.gradle.integration.tasks

import com.palantir.gradle.docker.DockerComposeUp
import com.xebialabs.gradle.integration.util.DbUtil
import org.apache.commons.io.IOUtils
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

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

        def resultComposeFilePath = Paths.get(
            "${project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString()}/${composeFile}")
        def parentDir = resultComposeFilePath.getParent().toFile()
        if (!parentDir.exists()) {
            parentDir.mkdirs()
        }
        def resultComposeFile = resultComposeFilePath.toFile()
        resultComposeFile.createNewFile()
        def os = new FileOutputStream(resultComposeFile)

        try {
            IOUtils.copy(dockerComposeStream, os)
        } finally {
            os.close()
        }

        return project.file(resultComposeFilePath)
    }

    @TaskAction
    void run() {
        super.run()
    }

}
