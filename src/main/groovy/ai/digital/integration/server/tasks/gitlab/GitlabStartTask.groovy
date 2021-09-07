package ai.digital.integration.server.tasks.gitlab

import ai.digital.integration.server.util.DockerComposeUtil
import ai.digital.integration.server.util.GitlabUtil
import ai.digital.integration.server.util.WaitForBootUtil
import com.palantir.gradle.docker.DockerComposeUp
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class GitlabStartTask extends DockerComposeUp {
    static NAME = "gitlabStart"

    GitlabStartTask() {
        this.group = PLUGIN_GROUP
    }

    @Override
    String getDescription() {
        "Starts gitlab instance using `docker-compose` and docker-compose-gitlab.yaml file."
    }

    @InputFiles
    File getDockerComposeFile() {
        project.file(DockerComposeUtil.getResolvedDockerPath(project, GitlabUtil.getGitlabRelativePath()))
    }

    @TaskAction
    void run() {
        project.logger.lifecycle("Starting GitLab server.")

        project.exec {
            it.executable "docker-compose"
            it.args '-f', getDockerComposeFile(), '-p', 'gitlabServer', 'up', '-d'
        }

        WaitForBootUtil.byPort(project, "GitLab server", "http://localhost:11180/", 11180) // TODO: port has to be configurable
    }

}
