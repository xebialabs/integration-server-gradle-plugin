package ai.digital.integration.server.tasks.gitlab

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

import static ai.digital.integration.server.constant.PluginConstant.DIST_DESTINATION_NAME
import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class GitlabStopTask extends DefaultTask {
    static NAME = 'gitlabStop'

    GitlabStopTask() {
        this.group = PLUGIN_GROUP
    }

    @InputFiles
    File getDockerComposeFile() {
        def composeFilePath = Paths.get(
                "${project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString()}" + "/docker-compose-gitlab.yml")
        composeFilePath.toFile()
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
