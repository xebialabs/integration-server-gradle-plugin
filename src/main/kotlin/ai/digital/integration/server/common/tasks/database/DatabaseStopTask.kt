package ai.digital.integration.server.common.tasks.database

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.DbUtil
import org.apache.tools.ant.taskdefs.condition.Os
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
        val resultComposeFilePath = DbUtil.getResolveDbFilePath(project)
        DbUtil.getResolvedDBDockerComposeFile(resultComposeFilePath, project)

        return project.file(resultComposeFilePath)
    }

    @TaskAction
    fun run() {
        // Use 'docker compose' on Windows, 'docker-compose' on other systems
        val executable = if (Os.isFamily(Os.FAMILY_WINDOWS)) "docker" else "docker-compose"
        val baseArgs = if (Os.isFamily(Os.FAMILY_WINDOWS)) listOf("compose") else emptyList()
        
        execOperations.exec {
            this.executable = executable
            args = baseArgs + arrayListOf("-f", getDockerComposeFile().path, "down", "--remove-orphans")
        }
    }
}
