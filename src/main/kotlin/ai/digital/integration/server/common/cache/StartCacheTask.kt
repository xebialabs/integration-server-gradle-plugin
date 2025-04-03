package ai.digital.integration.server.common.cache

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.util.CacheUtil
import ai.digital.integration.server.deploy.tasks.cli.DownloadAndExtractCliDistTask
import com.palantir.gradle.docker.DockerComposeUp
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

abstract class StartCacheTask @Inject constructor(
    private val execOperations: ExecOperations
) : DockerComposeUp() {

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
        project.logger.lifecycle("Starting Cache Server.")

        execOperations.exec {
            executable("docker-compose")
            args(arrayListOf("-f",
                    dockerComposeFile.path,
                    "--project-directory",
                    CacheUtil.getBaseDirectory(project),
                    "up",
                    "-d")
            )
        }

    }
}
