package ai.digital.integration.server.common.tasks.database

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.DbUtil
import ai.digital.integration.server.common.util.FileUtil
import ai.digital.integration.server.common.util.IntegrationServerUtil
import ai.digital.integration.server.deploy.tasks.server.ApplicationConfigurationOverrideTask
import com.palantir.gradle.docker.DockerComposeUp
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

abstract class DatabaseStartTask : DockerComposeUp() {

    companion object {
        const val NAME = "databaseStart"
    }

    init {
        this.group = PLUGIN_GROUP
        this.mustRunAfter(ApplicationConfigurationOverrideTask.NAME)
    }

    override fun getDescription(): String {
        return "Starts database instance using `docker-compose` and ${DbUtil.dockerComposeFileName(project)} file."
    }

    @InputFiles
    override fun getDockerComposeFile(): File {
        DbUtil.assertNotDerby(project, "Docker compose tasks do not support Derby database.")
        val resultComposeFilePath = DbUtil.getResolveDbFilePath(project)

        val src = DatabaseStartTask::class.java.protectionDomain.codeSource

        src?.let { codeSource ->
            val dbName = DbUtil.databaseName(project)
            File(resultComposeFilePath.parent.toFile().path, "$dbName-docker").mkdirs()

            val jar = codeSource.location
            val zip = ZipInputStream(jar.openStream())
            while (true) {
                val entry: ZipEntry? = zip.nextEntry

                if (entry != null) {
                    val name = entry.name

                    val folderName = "database-compose/$dbName-docker/"
                    if (name.startsWith(folderName) && name != folderName) {
                        val dockerFileName = name.substring(name.indexOf('/') + 1)
                        FileUtil.copyFile(zip,
                            IntegrationServerUtil.getRelativePathInIntegrationServerDist(project, dockerFileName))
                    }
                } else {
                    break
                }
            }
        }
        DbUtil.getResolvedDBDockerComposeFile(resultComposeFilePath, project)

        return project.file(resultComposeFilePath)
    }

    @TaskAction
    override fun run() {
        super.run()
    }
}
