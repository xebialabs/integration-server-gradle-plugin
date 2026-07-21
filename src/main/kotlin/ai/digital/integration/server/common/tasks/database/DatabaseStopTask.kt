package ai.digital.integration.server.common.tasks.database

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.DbUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

open class DatabaseStopTask @Inject constructor(
    private val execOperations: ExecOperations) : DefaultTask() {

    companion object {
        const val NAME = "databaseStop"
    }

    init {
        this.group = PLUGIN_GROUP
    }

    @Internal
    fun getDockerComposeFile(): File? {
        val dbName = DbUtil.databaseName(project)
        if (DbUtil.isEmbeddedDatabase(dbName)) {
            return null
        }
        val resultComposeFilePath = DbUtil.getResolveDbFilePath(project)
        DbUtil.getResolvedDBDockerComposeFile(resultComposeFilePath, project)

        return project.file(resultComposeFilePath)
    }

    @TaskAction
    fun run() {
        val dbName = DbUtil.databaseName(project)
        
        // Skip docker-compose for embedded databases like H2
        if (DbUtil.isEmbeddedDatabase(dbName)) {
            project.logger.lifecycle("Using embedded database $dbName - skipping docker-compose teardown.")
            return
        }
        
        execOperations.exec {
            executable = "docker-compose"
            args = arrayListOf("-f", getDockerComposeFile()!!.path, "down", "--remove-orphans")
        }
    }
}
