package ai.digital.integration.server.deploy.tasks.server.docker

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.util.DockerComposeUtil
import ai.digital.integration.server.deploy.domain.CentralConfigurationStandalone
import ai.digital.integration.server.deploy.internals.CentralConfigurationStandaloneUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.tasks.centralConfigurationStandalone.StartCCServerTask
import ai.digital.integration.server.deploy.tasks.server.PrepareServerTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File

open class DockerBasedStopCCTask : DefaultTask() {

    companion object {
        const val NAME = "dockerBasedStopCentralConfigServer"
    }

    init {
        this.dependsOn(StartCCServerTask.NAME)
        this.group = PLUGIN_GROUP
        this.onlyIf { CentralConfigurationStandaloneUtil.isDockerBased(project) }
    }

    @InputFiles
    fun getDockerComposeFile(cc: CentralConfigurationStandalone): File {
        return CentralConfigurationStandaloneUtil.getResolvedDockerFile(project, cc).toFile()
    }

    @TaskAction
    fun run() {
        project.exec {
            executable = "docker-compose"
            args = arrayListOf("-f", getDockerComposeFile(CentralConfigurationStandaloneUtil.getCC(project)).path, "down", "-v")
        }

    }
}
