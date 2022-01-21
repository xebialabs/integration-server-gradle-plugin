package ai.digital.integration.server.release.tasks.server.operator

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.util.WaitForBootUtil
import ai.digital.integration.server.release.tasks.DockerBasedStopReleaseTask
import ai.digital.integration.server.release.util.ReleaseServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class StartReleaseToGetLicenceTask : DefaultTask() {
    companion object {
        const val NAME = "startReleaseToGetLicence"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        this.finalizedBy(DockerBasedStopReleaseTask.NAME)
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

        val licenseFile = ReleaseServerUtil.getLicenseFile(project)
        WaitForBootUtil.byFile(project, process, licenseFile)
    }
}
