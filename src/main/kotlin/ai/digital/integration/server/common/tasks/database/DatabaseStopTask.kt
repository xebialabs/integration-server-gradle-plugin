package ai.digital.integration.server.common.tasks.database

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.DbUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
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

    @InputFiles
    fun getDockerComposeFile(): File {
        DbUtil.assertNotDerby(project, "Docker compose tasks do not support Derby database.")
        val resultComposeFilePath = DbUtil.getResolveDbFilePath(project)
        DbUtil.getResolvedDBDockerComposeFile(resultComposeFilePath, project)

        return project.file(resultComposeFilePath)
    }

    @TaskAction
    fun run() {
        execOperations.exec {
            executable = "docker-compose"
            args = arrayListOf("-f", getDockerComposeFile().path, "down", "--remove-orphans")
        }
    }
}
