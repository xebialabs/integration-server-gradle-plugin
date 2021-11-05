package ai.digital.integration.server.release.tasks

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.release.util.ReleaseServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.ByteArrayOutputStream
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

    /**
     * Ignoring an exception as only certain folders and files (which were mounted) belong to a docker user.
     */
    private fun allowToCleanMountedFiles() {
        project.exec {
            it.executable = "docker-compose"
            it.args = arrayListOf("-f",
                getDockerComposeFile().path,
                "exec",
                "-T",
                ReleaseServerUtil.getDockerServiceName(project),
                "chmod",
                "766",
                "-R",
                "/opt/xebialabs/xl-release-server")
            it.errorOutput = ByteArrayOutputStream()
            it.isIgnoreExitValue = true
        }
    }

    @TaskAction
    fun run() {
        project.logger.lifecycle("Stopping Release Server from a docker image ${
            ReleaseServerUtil.getDockerImageVersion(project)
        }")

        allowToCleanMountedFiles()

        project.exec {
            it.executable = "docker-compose"
            it.args = arrayListOf("-f", getDockerComposeFile().path, "down", "-v")
        }
    }
}
