package ai.digital.integration.server.tasks.database

import ai.digital.integration.server.tasks.ApplicationConfigurationOverrideTask
import ai.digital.integration.server.util.DbUtil
import ai.digital.integration.server.util.DeployServerUtil
import ai.digital.integration.server.util.FileUtil
import com.palantir.gradle.docker.DockerComposeUp
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

import java.util.zip.ZipInputStream

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class DatabaseStartTask extends DockerComposeUp {
    public static String NAME = "databaseStart"

    DatabaseStartTask() {
        this.configure { ->
            group = PLUGIN_GROUP
            mustRunAfter ApplicationConfigurationOverrideTask.NAME
        }
    }

    @Override
    String getDescription() {
        "Starts database instance using `docker-compose` and ${DbUtil.dockerComposeFileName(project).toString()} file."
    }

    @InputFiles
    File getDockerComposeFile() {
        DbUtil.assertNotDerby(project, 'Docker compose tasks do not support Derby database.')
        def resultComposeFilePath = DbUtil.getResolveDbFilePath(project)

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
                    FileUtil.copyFile(zip, DeployServerUtil.getRelativePathInIntegrationServerDist(project, dockerFileName))
                }
            }
        }
        project.file(resultComposeFilePath)
    }

    @TaskAction
    void run() {
        super.run()
    }

}
