package com.xebialabs.gradle.integration.tasks.database

import com.palantir.gradle.docker.DockerComposeUp
import com.xebialabs.gradle.integration.util.DbUtil
import com.xebialabs.gradle.integration.util.DockerComposeUtil
import com.xebialabs.gradle.integration.util.FileUtil
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

import java.util.zip.ZipInputStream

import static com.xebialabs.gradle.integration.constant.PluginConstant.PLUGIN_GROUP

class DatabaseStartTask extends DockerComposeUp {
    static NAME = "databaseStart"

    DatabaseStartTask() {
        this.group = PLUGIN_GROUP
    }

    @Override
    String getDescription() {
        return "Starts database instance using `docker-compose` and ${DbUtil.dockerComposeFileName(project).toString()} file."
    }

    private def getResolveDbFilePath() {
        def composeFileName = DbUtil.dockerComposeFileName(project)
        return DockerComposeUtil.getResolvedDockerPath(project, "database-compose/$composeFileName")
    }

    @InputFiles
    File getDockerComposeFile() {
        DbUtil.assertNotDerby(project, 'Docker compose tasks do not support Derby database.')
        def resultComposeFilePath = getResolveDbFilePath()

        def src = DatabaseStartTask.class.getProtectionDomain().getCodeSource()
        if (src != null) {
            def dbName = DbUtil.databaseName(project)
            new File("$dbName-docker", resultComposeFilePath.getParent().toFile()).mkdirs()

            URL jar = src.getLocation()
            def zip = new ZipInputStream(jar.openStream())
            while (true) {
                def e = zip.getNextEntry()
                if (e == null)
                    break
                String name = e.getName()

                def folderName = "database-compose/$dbName-docker/"
                if (name.startsWith(folderName) && name != folderName) {
                    def dockerFileName = name.substring(name.indexOf('/') + 1)
                    FileUtil.copyFile(zip, DockerComposeUtil.dockerfileDestination(project, dockerFileName))
                }
            }
        }
        return project.file(resultComposeFilePath)
    }

    @TaskAction
    void run() {
        super.run()
    }

}
