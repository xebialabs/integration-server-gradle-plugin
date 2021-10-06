package ai.digital.integration.server.util

import ai.digital.integration.server.domain.Satellite
import org.gradle.api.Project
import java.io.File
import java.nio.file.Paths
import kotlin.system.exitProcess

class SatelliteUtil {
    companion object {
        @JvmStatic
        fun hasSatellites(project: Project): Boolean {
            return getSatellites(project).isNotEmpty()
        }

        @JvmStatic
        fun getSatellites(project: Project): List<Satellite> {
            return ExtensionUtil.getExtension(project).satellites.map { satellite: Satellite ->
                enrichSatellite(project, satellite)
            }
        }

        @JvmStatic
        private fun enrichSatellite(project: Project, satellite: Satellite): Satellite {
            satellite.debugPort = getDebugPort(project, satellite)
            satellite.version = getSatelliteVersion(project, satellite)
            return satellite
        }

        @JvmStatic
        fun getSatelliteWorkingDir(project: Project, satellite: Satellite): String {
            val targetDir = IntegrationServerUtil.getDist(project)
            return Paths.get(targetDir, satellite.name, "xl-satellite-server-${satellite.version}").toAbsolutePath()
                .toString()
        }

        @JvmStatic
        fun getBinDir(project: Project, satellite: Satellite): File {
            return Paths.get(getSatelliteWorkingDir(project, satellite), "bin").toFile()
        }

        @JvmStatic
        fun getSatelliteLogDir(project: Project, satellite: Satellite): File {
            return project.file("${getSatelliteWorkingDir(project, satellite)}/log")
        }

        @JvmStatic
        fun getSatelliteLog(project: Project, satellite: Satellite): File {
            return project.file("${getSatelliteLogDir(project, satellite)}/xl-satellite.log")
        }

        @JvmStatic
        fun getSatelliteConf(project: Project, satellite: Satellite): File {
            return project.file("${getSatelliteWorkingDir(project, satellite)}/conf/satellite.conf")
        }

        @JvmStatic
        private fun getSatelliteVersion(project: Project, satellite: Satellite): String? {
            return if (project.hasProperty("xlSatelliteVersion")) {
                project.property("xlSatelliteVersion").toString()
            } else if (!satellite.version.isNullOrBlank()) {
                satellite.version
            } else if (!DeployServerUtil.getServer(project).version.isNullOrBlank()) {
                DeployServerUtil.getServer(project).version
            } else {
                project.logger.error("Satellite Version is not specified")
                exitProcess(1)
            }
        }

        @JvmStatic
        private fun getDebugPort(project: Project, satellite: Satellite): Int? {
            return if (PropertyUtil.resolveBooleanValue(project, "debug", true)) {
                PropertyUtil.resolveIntValue(project, "satelliteDebugPort", satellite.debugPort)
            } else {
                null
            }
        }
    }
}
