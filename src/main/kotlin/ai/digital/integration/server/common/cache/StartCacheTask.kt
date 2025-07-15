package ai.digital.integration.server.common.cache

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.util.CacheUtil
import ai.digital.integration.server.deploy.tasks.cli.DownloadAndExtractCliDistTask
import com.palantir.gradle.docker.DockerComposeUp
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class StartCacheTask: DockerComposeUp() {

    companion object {
        const val NAME = "startCache"
    }

    init {
        this.group = PluginConstant.PLUGIN_GROUP
        this.onlyIf {
            CacheUtil.isCacheEnabled(project)
        }
        this.mustRunAfter(DownloadAndExtractCliDistTask.NAME)
    }

    override fun getDescription(): String {
        return "Starts Cache Server using `docker-compose` and ${CacheUtil.getComposeFileRelativePath()} file."
    }

    @InputFiles
    override fun getDockerComposeFile(): File {
        return project.file(CacheUtil.getResolvedDockerFile(project))
    }

    @TaskAction
    override fun run() {
        project.logger.lifecycle("Cleaning up previous Cache containers and networks.")
        project.exec {
            executable = "docker-compose"
            args = arrayListOf("-f",
                getDockerComposeFile().path,
                "--project-directory",
                CacheUtil.getBaseDirectory(project),
                "down")
        }
        project.logger.lifecycle("Starting Cache Server.")

        project.exec {
            executable = "docker-compose"
            args = arrayListOf("-f",
                    dockerComposeFile.path,
                    "--project-directory",
                    CacheUtil.getBaseDirectory(project),
                    "up",
                    "-d")
        }

    }
}
