package ai.digital.integration.server.common.tasks.database

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.DbUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File

open class DatabaseStopTask : DefaultTask() {

    companion object {
        const val NAME = "databaseStop"
    }

    init {
        this.group = PLUGIN_GROUP
    }

    @InputFiles
    fun getDockerComposeFile(): File {
        DbUtil.assertNotDerby(project, "Docker compose tasks do not support Derby database.")
        return DbUtil.getResolveDbFilePath(project).toFile()
    }

    @TaskAction
    fun run() {
        project.exec {
            it.executable = "docker-compose"
            it.args = arrayListOf("-f", getDockerComposeFile().path, "down", "--remove-orphans")
        }
    }
}