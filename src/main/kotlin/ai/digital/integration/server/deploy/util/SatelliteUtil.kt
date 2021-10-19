package ai.digital.integration.server.deploy.util

import ai.digital.integration.server.common.util.IntegrationServerUtil
import ai.digital.integration.server.common.util.PropertyUtil
import ai.digital.integration.server.deploy.domain.Satellite
import org.gradle.api.Project
import java.io.File
import java.nio.file.Paths
import kotlin.system.exitProcess

class SatelliteUtil {
    companion object {
        fun hasSatellites(project: Project): Boolean {
            return getSatellites(project).isNotEmpty()
        }

        fun getSatellites(project: Project): List<Satellite> {
            return DeployExtensionUtil.getExtension(project).satellites.map { satellite: Satellite ->
                enrichSatellite(project, satellite)
            }
        }

        private fun enrichSatellite(project: Project, satellite: Satellite): Satellite {
            satellite.debugPort = getDebugPort(project, satellite)
            satellite.version = getSatelliteVersion(project, satellite)
            return satellite
        }

        fun getSatelliteWorkingDir(project: Project, satellite: Satellite): String {
            val targetDir = IntegrationServerUtil.getDist(project)
            return Paths.get(targetDir, satellite.name, "xl-satellite-server-${satellite.version}").toAbsolutePath()
                .toString()
        }

        fun getBinDir(project: Project, satellite: Satellite): File {
            return Paths.get(getSatelliteWorkingDir(project, satellite), "bin").toFile()
        }

        fun getSatelliteLogDir(project: Project, satellite: Satellite): File {
            return project.file("${getSatelliteWorkingDir(project, satellite)}/log")
        }

        fun getSatelliteLog(project: Project, satellite: Satellite): File {
            return project.file("${getSatelliteLogDir(project, satellite)}/xl-satellite.log")
        }

        fun getSatelliteConf(project: Project, satellite: Satellite): File {
            return project.file("${getSatelliteWorkingDir(project, satellite)}/conf/satellite.conf")
        }

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

        private fun getDebugPort(project: Project, satellite: Satellite): Int? {
            return if (PropertyUtil.resolveBooleanValue(project, "debug", true)) {
                PropertyUtil.resolveIntValue(project, "satelliteDebugPort", satellite.debugPort)
            } else {
                null
            }
        }
    }
}
