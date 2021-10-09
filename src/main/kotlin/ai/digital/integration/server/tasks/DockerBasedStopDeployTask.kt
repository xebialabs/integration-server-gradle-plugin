package ai.digital.integration.server.tasks

import ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.util.DeployServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.ByteArrayOutputStream
import java.io.File

abstract class DockerBasedStopDeployTask : DefaultTask() {

    companion object {
        @JvmStatic
        val NAME = "dockerBasedStopDeploy"
    }

    init {
        this.dependsOn(PrepareDeployTask.NAME)
        this.group = PLUGIN_GROUP
        this.onlyIf { DeployServerUtil.isDockerBased(project) }
    }

    @InputFiles
    fun getDockerComposeFile(): File {
        return DeployServerUtil.getResolvedDockerFile(project).toFile()
    }

    /**
     * Ignoring an exception as only certain folders and files (which were mounted) belong to a docker user.
     */
    fun allowToCleanMountedFiles() {
        project.exec {
            it.executable = "docker-compose"
            it.args = arrayListOf("-f",
                getDockerComposeFile().path,
                "exec",
                "-T",
                DeployServerUtil.getDockerServiceName(project),
                "chmod",
                "777",
                "-R",
                "/opt/xebialabs/xl-deploy-server")
            it.errorOutput = ByteArrayOutputStream()
            it.isIgnoreExitValue = true
        }
    }

    @TaskAction
    fun run() {
        project.logger.lifecycle("Stopping Deploy Server from a docker image ${
            DeployServerUtil.getDockerImageVersion(project)
        }")

        allowToCleanMountedFiles()

        project.exec {
            it.executable = "docker-compose"
            it.args = arrayListOf("-f", getDockerComposeFile().path, "down", "-v")
        }
    }
}
