package ai.digital.integration.server.tasks.gitlab

import ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.util.DockerComposeUtil
import ai.digital.integration.server.util.GitlabUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GitlabStopTask : DefaultTask() {

    init {
        this.group = PLUGIN_GROUP
    }

    @InputFiles
    fun getDockerComposeFile(): File {
        return project.file(DockerComposeUtil.getResolvedDockerPath(project, GitlabUtil.getGitlabRelativePath()))
    }

    @TaskAction
    fun run() {
        project.logger.lifecycle("Stopping GitLab server.")

        project.exec {
            it.executable = "docker-compose"
            it.args = listOf("-f", getDockerComposeFile().toString(), "-p", "gitlabServer", "down")
        }
    }

    companion object {
        @JvmStatic
        val NAME = "gitlabStop"
    }
}
