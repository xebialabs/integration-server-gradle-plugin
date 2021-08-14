package ai.digital.integration.server.tasks

import ai.digital.integration.server.util.ServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class DockerBasedStartDeployTask extends DefaultTask {
    static NAME = "dockerBasedStartDeploy"

    DockerBasedStartDeployTask() {

        def dependencies = [
                DockerBasedStopDeployTask.NAME,
                PrepareDeployTask.NAME
        ]

        this.configure {
            this.group = PLUGIN_GROUP
            dependsOn(dependencies)
            onlyIf { ServerUtil.isDockerBased(project) }
        }
    }

    @InputFiles
    File getDockerComposeFile() {
        project.file(ServerUtil.getResolvedDockerFile(project))
    }

    @TaskAction
    void run() {
        project.logger.lifecycle("Starting Deploy Server from a docker image ${ServerUtil.getDockerImageVersion(project)}")
        project.exec {
            it.executable "docker-compose"
            it.args '-f', getDockerComposeFile(), '-p', 'deployServer', 'up', '-d'
        }
    }
}
