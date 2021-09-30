package ai.digital.integration.server.tasks.gitlab

import ai.digital.integration.server.util.DockerComposeUtil
import ai.digital.integration.server.util.GitlabUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class GitlabStopTask extends DefaultTask {
    public static String NAME = 'gitlabStop'

    GitlabStopTask() {
        this.group = PLUGIN_GROUP
    }

    @InputFiles
    File getDockerComposeFile() {
        project.file(DockerComposeUtil.getResolvedDockerPath(project, GitlabUtil.getGitlabRelativePath()))
    }

    @TaskAction
    void run() {
        project.logger.lifecycle("Stopping GitLab server.")

        project.exec {
            it.executable 'docker-compose'
            it.args '-f', getDockerComposeFile(), '-p', 'gitlabServer', 'down'
        }
    }
}
