package ai.digital.integration.server.common.cache

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.util.CacheUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.internals.WorkerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File

open class ShutdownCacheTask: DefaultTask() {

    companion object {
        const val NAME = "shutdownCache"
    }

    init {
        this.group = PluginConstant.PLUGIN_GROUP
        this.onlyIf {
            DeployServerUtil.getServers(project).size > 1 || WorkerUtil.hasWorkers(project)
        }
    }

    @InputFiles
    fun getDockerComposeFile(): File {
        return CacheUtil.getResolvedDockerFile(project).toFile()
    }

    @TaskAction
    fun stop() {
        project.logger.lifecycle("Shutting down Cache Server.")

        project.exec {
            executable = "docker-compose"
            args = arrayListOf("-f",
                    getDockerComposeFile().path,
                    "--project-directory",
                    CacheUtil.getBaseDirectory(project),
                    "down")
        }
    }
}