package ai.digital.integration.server.release.tasks.server.operator

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.util.DockerComposeUtil
import ai.digital.integration.server.common.util.WaitForBootUtil
import ai.digital.integration.server.release.internals.ReleaseServerInitializeUtil
import ai.digital.integration.server.release.tasks.DockerBasedStopReleaseTask
import ai.digital.integration.server.release.util.ReleaseServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import org.gradle.process.ExecOperations
import javax.inject.Inject

open class StartReleaseToGetLicenceTask @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {
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

    private fun getDockerComposeFile(): File {
        return ReleaseServerUtil.getResolvedDockerFile(project).toFile()
    }

    private fun start(): Process? {
        execOperations.exec {
            executable = "docker-compose"
            args = listOf("-f", getDockerComposeFile().toString(), "up", "-d")
        }
        return null
    }

    @TaskAction
    fun launch() {
        val server = ReleaseServerUtil.getServer(project)
        ReleaseServerInitializeUtil.prepare(project, server)

        project.logger.lifecycle("About to launch Release Server on port " + server.httpPort.toString() + ".")
        allowToWriteMountedHostFolders()
        val process = start()

        val licenseFile = ReleaseServerUtil.getLicenseFile(project)
        WaitForBootUtil.byFile(project, process, licenseFile)

        DockerComposeUtil.allowToCleanMountedFiles(project, ProductName.RELEASE, server, getDockerComposeFile())
    }
}
