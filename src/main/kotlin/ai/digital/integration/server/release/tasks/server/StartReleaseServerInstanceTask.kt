package ai.digital.integration.server.release.tasks.server

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.release.util.ReleaseServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class StartReleaseServerInstanceTask : DefaultTask() {
    companion object {
        const val NAME = "startReleaseServerInstance"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
    }

    private fun allowToWriteMountedHostFolders() {
        ReleaseServerUtil.grantPermissionsToIntegrationServerFolder(project)
    }

    private fun start(): Process? {
        project.exec {
            executable = "docker-compose"
            args = listOf("-f", ReleaseServerUtil.getResolvedDockerFile(project).toFile().toString(), "up", "-d")
        }
        return null
    }

    @TaskAction
    fun launch() {
        val server = ReleaseServerUtil.getServer(project)
        project.logger.lifecycle("About to launch Release Server on port " + server.httpPort.toString() + ".")
        allowToWriteMountedHostFolders()
        val process = start()
        ReleaseServerUtil.waitForBoot(project, process)
    }
}
