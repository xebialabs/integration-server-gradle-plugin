package ai.digital.integration.server.deploy.tasks.server.docker

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.domain.Server
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
    fun getDockerComposeFile(server: Server): File {
        return DeployServerUtil.getResolvedDockerFile(project, server).toFile()
    }

    @TaskAction
    fun run() {
        DeployServerUtil.getServers(project)
            .forEach { server ->
                project.logger.lifecycle("Stopping Deploy Server from a docker image ${
                    DeployServerUtil.getDockerImageVersion(server)
                }")
                DockerComposeUtil.allowToCleanMountedFiles(project, server, getDockerComposeFile(server))
                project.exec {
                    executable = "docker-compose"
                    args = arrayListOf("-f", getDockerComposeFile(server).path, "down", "-v")
                }
            }
    }
}
