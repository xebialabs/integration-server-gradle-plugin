package ai.digital.integration.server.deploy.tasks.server.docker

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.util.DockerComposeUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.tasks.server.PrepareServerTask
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

open class DockerBasedStopDeployTask @Inject constructor(
    private val execOperations: ExecOperations) : DefaultTask() {

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
        // Use 'docker compose' on Windows, 'docker-compose' on other systems
        val executable = if (Os.isFamily(Os.FAMILY_WINDOWS)) "docker" else "docker-compose"
        val baseArgs = if (Os.isFamily(Os.FAMILY_WINDOWS)) listOf("compose") else emptyList()
        
        DeployServerUtil.getServers(project)
            .forEach { server ->
                project.logger.lifecycle("Stopping Deploy Server from a docker image ${
                    DeployServerUtil.getDockerImageVersion(server)
                }")
                DockerComposeUtil.allowToCleanMountedFiles(project, ProductName.DEPLOY, server, getDockerComposeFile(server))
                execOperations.exec {
                    this.executable = executable
                    args = baseArgs + arrayListOf("-f", getDockerComposeFile(server).path, "down", "-v")
                }
            }
    }
}
