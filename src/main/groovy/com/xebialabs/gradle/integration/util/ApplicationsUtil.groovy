package com.xebialabs.gradle.integration.util

import com.xebialabs.gradle.integration.tasks.ShutdownIntegrationServerTask
import com.xebialabs.gradle.integration.tasks.StartIntegrationServerTask
import com.xebialabs.gradle.integration.tasks.centralconfig.ShutDownConfigServerTask
import com.xebialabs.gradle.integration.tasks.centralconfig.StartConfigServerTask
import com.xebialabs.gradle.integration.tasks.gitlab.DockerComposeGitlabStartTask
import com.xebialabs.gradle.integration.tasks.gitlab.DockerComposeGitlabStopTask
import com.xebialabs.gradle.integration.tasks.satellite.StartSatelliteTask
import com.xebialabs.gradle.integration.tasks.satellite.StopSatelliteTask

class ApplicationsUtil {
    static def XLD_START = 'xldServerStart'
    static def XLD_STOP = 'xldServerStop'
    static def SATELLITE_START = 'satelliteServerStart'
    static def SATELLITE_STOP = 'satelliteServerStop'
    static def CONFIG_SERVER_START = 'configServerStart'
    static def CONFIG_SERVER_STOP = 'configServerStop'
    static def GITLAB_START = 'gitlabStart'
    static def GITLAB_STOP = 'gitlabStop'

    static def applicationName(project) {
        project.hasProperty("application") ? project.property("application").toString() : GITLAB_START
    }

    static def detectApplication(application) {
        switch (application) {
            case XLD_START: return StartIntegrationServerTask.NAME
            case XLD_STOP: return ShutdownIntegrationServerTask.NAME
            case GITLAB_START: return DockerComposeGitlabStartTask.NAME
            case GITLAB_STOP: return DockerComposeGitlabStopTask.NAME
            case SATELLITE_START: return StartSatelliteTask.NAME
            case SATELLITE_STOP: return StopSatelliteTask.NAME
            case CONFIG_SERVER_START: return StartConfigServerTask.NAME
            case CONFIG_SERVER_STOP: return ShutDownConfigServerTask.NAME
            default: return StartIntegrationServerTask.NAME
        }
    }
}

