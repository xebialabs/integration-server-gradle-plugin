package ai.digital.integration.server.tasks

import ai.digital.integration.server.util.DeployServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class DockerBasedStopDeployTask extends DefaultTask {
    public static String NAME = "dockerBasedStopDeploy"

    DockerBasedStopDeployTask() {

        def dependencies = [
                PrepareDeployTask.NAME
        ]

        this.configure {
            this.group = PLUGIN_GROUP
            dependsOn(dependencies)
            onlyIf { DeployServerUtil.isDockerBased(project) }
        }
    }

    @InputFiles
    File getDockerComposeFile() {
        DeployServerUtil.getResolvedDockerFile(project).toFile()
    }

    /**
     * Ignoring an exception as only certain folders and files (which were mounted) belong to a docker user.
     */
    def allowToCleanMountedFiles() {
        project.exec {
            it.executable 'docker-compose'
            it.args '-f', getDockerComposeFile(), 'exec', '-T', DeployServerUtil.getDockerServiceName(project),
                    'chmod', '777', '-R', '/opt/xebialabs/xl-deploy-server'
            errorOutput = new ByteArrayOutputStream()
            ignoreExitValue = true
        }
    }

    @TaskAction
    void run() {
        project.logger.lifecycle("Stopping Deploy Server from a docker image ${DeployServerUtil.getDockerImageVersion(project)}")

        allowToCleanMountedFiles()

        project.exec {
            it.executable "docker-compose"
            it.args '-f', getDockerComposeFile(), 'down', '-v'
        }
    }
}
