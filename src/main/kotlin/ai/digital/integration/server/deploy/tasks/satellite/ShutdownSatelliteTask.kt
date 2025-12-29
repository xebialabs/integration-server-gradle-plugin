package ai.digital.integration.server.deploy.tasks.satellite

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.deploy.domain.Satellite
import ai.digital.integration.server.common.util.FileUtil
import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.deploy.internals.SatelliteUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission

open class ShutdownSatelliteTask : DefaultTask() {

    init {
        this.group = PLUGIN_GROUP
    }

    private fun copyStopSatelliteScript(satellite: Satellite) {
        val from = {}::class.java.classLoader.getResourceAsStream("satellite/bin/$STOP_SATELLITE_SCRIPT")
        val intoDir = Paths.get(SatelliteUtil.getSatelliteWorkingDir(project, satellite)).resolve(STOP_SATELLITE_SCRIPT)

        from?.let { source ->
            FileUtil.copyFile(source, intoDir)
            // Set executable permissions (rwxr-xr-x / 755)
            setExecutablePermissions(intoDir)
        }
    }

    private fun setExecutablePermissions(filePath: java.nio.file.Path) {
        try {
            if (Files.exists(filePath)) {
                val permissions = setOf(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE,
                    PosixFilePermission.GROUP_READ,
                    PosixFilePermission.GROUP_EXECUTE,
                    PosixFilePermission.OTHERS_READ,
                    PosixFilePermission.OTHERS_EXECUTE
                )
                Files.setPosixFilePermissions(filePath, permissions)
            }
        } catch (e: UnsupportedOperationException) {
            // On Windows, POSIX permissions are not supported, skip silently
            project.logger.debug("POSIX permissions not supported on this platform: ${e.message}")
        }
    }

    private fun stopSatellite(satellite: Satellite) {
        ProcessUtil.exec(mapOf<String, Any?>(
            "command" to "stopSatellite",
            "workDir" to File(SatelliteUtil.getSatelliteWorkingDir(project, satellite))
        ))
        project.logger.lifecycle("Satellite server '${satellite.name}' successfully shutdown.")
    }


    @TaskAction
    fun stop() {
        SatelliteUtil.getSatellites(project).forEach { satellite ->
            project.logger.lifecycle("Shutting down satellite ${satellite.name}")
            copyStopSatelliteScript(satellite)
            stopSatellite(satellite)
        }
    }

    companion object {
        const val NAME = "shutdownSatellite"
        const val STOP_SATELLITE_SCRIPT = "stopSatellite.sh"
    }
}
