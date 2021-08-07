package com.xebialabs.gradle.integration.tasks.gitlab

import com.palantir.gradle.docker.DockerComposeUp
import com.xebialabs.gradle.integration.util.DockerComposeUtil
import com.xebialabs.gradle.integration.util.WaitForBootUtil
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.constant.PluginConstant.PLUGIN_GROUP

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
        def relativePath = "gitlab/gitlab-compose/docker-compose-gitlab.yml"
        project.file(DockerComposeUtil.getResolvedDockerPath(project, relativePath))
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
