package ai.digital.integration.server.common.gitlab

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.DockerComposeUtil
import ai.digital.integration.server.common.util.GitlabUtil
import ai.digital.integration.server.deploy.tasks.cli.DownloadAndExtractCliDistTask
import ai.digital.integration.server.deploy.tasks.server.DownloadAndExtractServerDistTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

open class GitlabStopTask @Inject constructor(
    private val execOperations: ExecOperations): DefaultTask() {

    init {
        this.group = PLUGIN_GROUP
        this.mustRunAfter(DownloadAndExtractServerDistTask.NAME, DownloadAndExtractCliDistTask.NAME)
    }

    @InputFiles
    fun getDockerComposeFile(): File {
        return project.file(DockerComposeUtil.getResolvedDockerPath(project, GitlabUtil.getGitlabRelativePath()))
    }

    @TaskAction
    fun run() {
        project.logger.lifecycle("Stopping GitLab server.")

        execOperations.exec {
            executable = "docker-compose"
            args = listOf("-f", getDockerComposeFile().toString(), "-p", "gitlab_server", "down")
        }
    }

    companion object {
        const val NAME = "gitlabStop"
    }
}
