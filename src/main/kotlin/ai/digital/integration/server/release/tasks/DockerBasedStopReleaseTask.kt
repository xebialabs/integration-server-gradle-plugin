package ai.digital.integration.server.release.tasks

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.util.DockerComposeUtil
import ai.digital.integration.server.release.util.ReleaseServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File

open class DockerBasedStopReleaseTask : DefaultTask() {

    companion object {
        const val NAME = "dockerBasedStopRelease"
    }

    init {
        this.group = PLUGIN_GROUP
        this.onlyIf { ReleaseServerUtil.isDockerBased(project) }
    }

    @InputFiles
    fun getDockerComposeFile(): File {
        return ReleaseServerUtil.getResolvedDockerFile(project).toFile()
    }

    @TaskAction
    fun run() {
        project.logger.lifecycle("Stopping Release Server from a docker image ${
            ReleaseServerUtil.getDockerImageVersion(project)
        }")

        val server = ReleaseServerUtil.getServer(project)
        DockerComposeUtil.allowToCleanMountedFiles(project, ProductName.RELEASE, server, getDockerComposeFile())

        project.exec {
            executable = "docker-compose"
            args = arrayListOf("-f", getDockerComposeFile().path, "down", "-v")
        }
    }
}
