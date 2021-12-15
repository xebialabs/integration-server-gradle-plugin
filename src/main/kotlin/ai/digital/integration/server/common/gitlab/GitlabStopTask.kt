package ai.digital.integration.server.common.gitlab

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.DockerComposeUtil
import ai.digital.integration.server.common.util.GitlabUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File

open class GitlabStopTask : DefaultTask() {

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
            executable = "docker-compose"
            args = listOf("-f", getDockerComposeFile().toString(), "-p", "gitlabServer", "down")
        }
    }

    companion object {
        const val NAME = "gitlabStop"
    }
}
