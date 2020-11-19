package com.xebialabs.gradle.integration.tasks.database

import com.palantir.gradle.docker.DockerComposeUp
import com.xebialabs.gradle.integration.util.DbUtil
import com.xebialabs.gradle.integration.util.DockerComposeUtil
import org.apache.commons.io.IOUtils
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

import java.util.zip.ZipInputStream

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class DockerComposeDatabaseStartTask extends DockerComposeUp {
    static NAME = "dockerComposeDatabaseStart"

    DockerComposeDatabaseStartTask() {
        this.group = PLUGIN_GROUP
    }

    @Override
    String getDescription() {
        return "Starts database instance using `docker-compose` and ${DockerComposeUtil.dockerComposeFileName(project).toString()} file."
    }

    @InputFiles
    File getDockerComposeFile() {
        DbUtil.assertNotDerby(project, 'Docker compose tasks do not support Derby database.')

        def composeFile = DockerComposeUtil.dockerComposeFileName(project)
        def dockerComposeStream = DockerComposeDatabaseStartTask.class.classLoader
                .getResourceAsStream("database-compose/${composeFile}")

        def resultComposeFilePath = DockerComposeUtil.dockerComposeFileDestination(project)
        def parentDir = resultComposeFilePath.getParent().toFile()
        parentDir.mkdirs()
        copyFile(dockerComposeStream, resultComposeFilePath)

        def src = DockerComposeDatabaseStartTask.class.getProtectionDomain().getCodeSource()
        if (src != null) {
            def dbName = DbUtil.databaseName(project)
            def folderName = "database-compose/$dbName/"
            def dockerfileDir = new File(dbName, parentDir)
            dockerfileDir.mkdirs()
            URL jar = src.getLocation()
            def zip = new ZipInputStream(jar.openStream())
            while (true) {
                def e = zip.getNextEntry()
                if (e == null)
                    break;
                String name = e.getName()
                if (name.startsWith(folderName) && name != folderName) {
                    copyFile(zip, DockerComposeUtil.dockerfileDestination(project, name.substring(name.indexOf('/') + 1)))
                }
            }
        }
        return project.file(resultComposeFilePath)
    }

    private static def copyFile(inputStream, path) {
        def file = path.toFile()
        def os = new FileOutputStream(file)
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs()
        }
        file.createNewFile()
        try {
            IOUtils.copy(inputStream, os)
        } finally {
            os.close()
        }
    }

    @TaskAction
    void run() {
        super.run()
    }

}
