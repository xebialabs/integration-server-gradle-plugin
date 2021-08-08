package com.xebialabs.gradle.integration.util


import com.xebialabs.gradle.integration.domain.Satellite
import org.gradle.api.Project

import java.nio.file.Paths

import static com.xebialabs.gradle.integration.constant.PluginConstant.DIST_DESTINATION_NAME

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
        satellite.setVersion(getServerVersion(project, satellite))
        satellite
    }

    static def getSatelliteWorkingDir(Project project, Satellite satellite) {
        def targetDir = project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString()
        Paths.get(targetDir, "xl-satellite-server-${satellite.version}").toAbsolutePath().toString()
    }

    static def getBinDir(Project project, Satellite satellite) {
        Paths.get(getSatelliteWorkingDir(project, satellite), "bin").toFile()
    }

    static def getSatelliteLog(Project project, Satellite satellite) {
        project.file("${getSatelliteWorkingDir(project, satellite)}/log/xl-satellite.log")
    }

    private static String getServerVersion(Project project, Satellite satellite) {
        project.hasProperty("xlSatelliteVersion") ? project.property("xlSatelliteVersion") : satellite.version
    }

    private static Integer getDebugPort(Project project, Satellite satellite) {
        project.hasProperty("satelliteDebugPort") ? Integer.valueOf(project.property("satelliteDebugPort").toString()) : satellite.debugPort
    }
}
