package ai.digital.integration.server.deploy.tasks.server.docker

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.DockerComposeUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.tasks.server.PrepareServerTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File

open class DockerBasedStopDeployTask : DefaultTask() {

    companion object {
        const val NAME = "dockerBasedStopDeploy"
    }

    init {
        this.dependsOn(PrepareServerTask.NAME)
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
    private fun allowToCleanMountedFiles() {
        val args = arrayListOf("-f",
            getDockerComposeFile().path,
            "exec",
            "-T",
            DeployServerUtil.getDockerServiceName(project),
            "chmod",
            "766",
            "-R",
            "/opt/xebialabs/xl-deploy-server")
        DockerComposeUtil.execute(project, args, true)
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
