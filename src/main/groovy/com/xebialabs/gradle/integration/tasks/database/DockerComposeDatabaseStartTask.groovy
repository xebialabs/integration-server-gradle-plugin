package com.xebialabs.gradle.integration.tasks.database

import com.palantir.gradle.docker.DockerComposeUp
import com.xebialabs.gradle.integration.util.DbUtil
import com.xebialabs.gradle.integration.util.DockerComposeUtil
import org.apache.commons.io.IOUtils
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

class DockerComposeDatabaseStartTask extends DockerComposeUp {
    static NAME = "dockerComposeDatabaseStart"

    @InputFiles
    File getDockerComposeFile() {
        DbUtil.assertNotDerby(project, 'Docker compose tasks do not support Derby database.')

        def composeFile = DockerComposeUtil.dockerComposeFileName(project)
        def dockerComposeStream = DockerComposeDatabaseStartTask.class.classLoader
            .getResourceAsStream("database-compose/${composeFile}")

        def resultComposeFilePath = DockerComposeUtil.dockerComposeFileDestination(project)
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
