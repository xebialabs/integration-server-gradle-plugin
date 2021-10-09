package ai.digital.integration.server.tasks.gitlab

import ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.util.DockerComposeUtil
import ai.digital.integration.server.util.GitlabUtil
import ai.digital.integration.server.util.WaitForBootUtil
import com.palantir.gradle.docker.DockerComposeUp
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GitlabStartTask : DockerComposeUp() {

    companion object {
        @JvmStatic
        val NAME = "gitlabStart"
    }

    init {
        this.group = PLUGIN_GROUP
    }

    override fun getDescription(): String {
        return "Starts gitlab instance using `docker-compose` and docker-compose-gitlab.yaml file."
    }

    @InputFiles
    override fun getDockerComposeFile(): File {
        return project.file(DockerComposeUtil.getResolvedDockerPath(project, GitlabUtil.getGitlabRelativePath()))
    }

    @TaskAction
    override fun run() {
        project.logger.lifecycle("Starting GitLab server.")

        project.exec {
            it.executable = "docker-compose"
            it.args = arrayListOf("-f", dockerComposeFile.path, "-p", "gitlabServer", "up", "-d")
        }

        WaitForBootUtil.byPort(project, "GitLab server", "http://localhost:11180/") // TODO: port has to be configurable
    }
}
