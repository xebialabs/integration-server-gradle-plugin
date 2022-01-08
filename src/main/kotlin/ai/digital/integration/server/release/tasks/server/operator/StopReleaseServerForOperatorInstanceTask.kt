package ai.digital.integration.server.release.tasks.server.operator

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.util.DockerComposeUtil
import ai.digital.integration.server.release.util.ReleaseServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class StopReleaseServerForOperatorInstanceTask : DefaultTask() {
    companion object {
        const val NAME = "stopReleaseServerForOperatorInstance"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        val server = ReleaseServerUtil.getServer(project)
        val dockerComposeFile = ReleaseServerUtil.getResolvedDockerFile(project).toFile()
        DockerComposeUtil.allowToCleanMountedFiles(project, server, dockerComposeFile)

        val args = listOf(
            "-f",
            dockerComposeFile.toString(),
            "down"
        )
        DockerComposeUtil.execute(project, args)
    }
}
