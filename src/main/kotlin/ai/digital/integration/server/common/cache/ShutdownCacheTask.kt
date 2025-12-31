package ai.digital.integration.server.common.cache

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.util.CacheUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

open class ShutdownCacheTask @Inject constructor(
    private val execOperations: ExecOperations) : DefaultTask()  {

    companion object {
        const val NAME = "shutdownCache"
    }

    init {
        this.group = PluginConstant.PLUGIN_GROUP
        this.onlyIf {
            CacheUtil.isCacheEnabled(project)
        }
    }

    @InputFiles
    fun getDockerComposeFile(): File {
        return CacheUtil.getResolvedDockerFile(project).toFile()
    }

    @TaskAction
    fun stop() {
        project.logger.lifecycle("Shutting down Cache Server.")

        execOperations.exec {
            executable = "docker-compose"
            args = arrayListOf("-f",
                    getDockerComposeFile().path,
                    "--project-directory",
                    CacheUtil.getBaseDirectory(project),
                    "down")
        }
    }
}