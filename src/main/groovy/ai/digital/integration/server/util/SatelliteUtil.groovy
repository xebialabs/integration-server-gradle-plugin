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
        def targetDir = ServerUtil.getIntegrationServerDist(project)
        Paths.get(targetDir, satellite.name, "xl-satellite-server-${satellite.version}").toAbsolutePath().toString()
    }

    static def getBinDir(Project project, Satellite satellite) {
        Paths.get(getSatelliteWorkingDir(project, satellite), "bin").toFile()
    }

    static def getSatelliteLog(Project project, Satellite satellite) {
        project.file("${getSatelliteWorkingDir(project, satellite)}/log/xl-satellite.log")
    }

    private static String getSatelliteVersion(Project project, Satellite satellite) {
        project.hasProperty("xlSatelliteVersion") ? project.property("xlSatelliteVersion") : satellite.version
    }

    private static Integer getDebugPort(Project project, Satellite satellite) {
        project.hasProperty("satelliteDebugPort") ? Integer.valueOf(project.property("satelliteDebugPort").toString()) : satellite.debugPort
    }
}
