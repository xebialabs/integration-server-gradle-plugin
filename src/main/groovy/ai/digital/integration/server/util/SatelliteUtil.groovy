package ai.digital.integration.server.util

import ai.digital.integration.server.domain.Satellite
import org.gradle.api.Project

import java.nio.file.Paths

class SatelliteUtil {

    static def hasSatellites(Project project) {
        getSatellites(project).size() > 0
    }

    static List<Satellite> getSatellites(Project project) {
        ExtensionUtil.getExtension(project).satellites.collect { Satellite satellite ->
            enrichSatellite(project, satellite)
        }
    }

    private static Satellite enrichSatellite(Project project, Satellite satellite) {
        satellite.setDebugPort(getDebugPort(project, satellite))
        satellite.setVersion(getSatelliteVersion(project, satellite))
        satellite
    }

    static def getSatelliteWorkingDir(Project project, Satellite satellite) {
        def targetDir = IntegrationServerUtil.getDist(project)
        Paths.get(targetDir, satellite.name, "xl-satellite-server-${satellite.version}").toAbsolutePath().toString()
    }

    static def getBinDir(Project project, Satellite satellite) {
        Paths.get(getSatelliteWorkingDir(project, satellite), "bin").toFile()
    }

    static def getSatelliteLog(Project project, Satellite satellite) {
        project.file("${getSatelliteWorkingDir(project, satellite)}/log/xl-satellite.log")
    }

    static def getSatelliteConf(Project project, Satellite satellite) {
        project.file("${getSatelliteWorkingDir(project, satellite)}/conf/satellite.conf")
    }

    private static String getSatelliteVersion(Project project, Satellite satellite) {
        if (project.hasProperty("xlSatelliteVersion")) {
            return project.getProperty("xlSatelliteVersion")
        } else if (satellite.version?.trim()) {
            return satellite.version
        } else if (DeployServerUtil.getServer(project).version) {
            return DeployServerUtil.getServer(project).version
        } else {
            project.logger.error("Satellite Version is not specified")
            System.exit(1)
            return null
        }
    }

    private static Integer getDebugPort(Project project, Satellite satellite) {
        if (PropertyUtil.resolveBooleanValue(project, "debug", true)) {
            PropertyUtil.resolveIntValue(project, "satelliteDebugPort", satellite.debugPort)
        } else {
            null
        }
    }
}
