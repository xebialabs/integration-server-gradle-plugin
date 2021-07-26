package com.xebialabs.gradle.integration.util

import com.xebialabs.gradle.integration.tasks.ShutdownIntegrationServerTask
import com.xebialabs.gradle.integration.tasks.StartIntegrationServerTask
import com.xebialabs.gradle.integration.tasks.centralconfig.ShutDownConfigServerTask
import com.xebialabs.gradle.integration.tasks.centralconfig.StartConfigServerTask
import com.xebialabs.gradle.integration.tasks.gitlab.DockerComposeGitlabStartTask
import com.xebialabs.gradle.integration.tasks.gitlab.DockerComposeGitlabStopTask
import com.xebialabs.gradle.integration.tasks.pluginManager.StartPluginManagerTask
import com.xebialabs.gradle.integration.tasks.satellite.ShutdownSatelliteTask
import com.xebialabs.gradle.integration.tasks.satellite.StartSatelliteTask
import com.xebialabs.gradle.integration.tasks.worker.ShutdownWorker
import com.xebialabs.gradle.integration.tasks.worker.StartWorker

class ApplicationsUtil {
    static def XLD_START = 'startXLDServer'
    static def XLD_STOP = 'shutdownXLDServer'
    static def SATELLITE_START = 'startSatelliteServer'
    static def SATELLITE_STOP = 'shutdownSatelliteServer'
    static def CONFIG_SERVER_START = 'startConfigServer'
    static def CONFIG_SERVER_STOP = 'shutdownConfigServer'
    static def GITLAB_START = 'startGitlab'
    static def GITLAB_STOP = 'shutdownGitlab'
    static def PLUGIN_MANAGER_START = 'pluginManagerCliStart'
    static def XLD_WITH_WORKER_START = 'startXLDWithExternalWorker'
    static def XLD_WITH_WORKER_STOP = 'stopXLDWithExternalWorker'

    static def applicationName(project) {
        def application = project.hasProperty("application") ? project.property("application").toString() : XLD_START
        def externalWorker = project.hasProperty("externalWorker") ? project.property("externalWorker") : false

        if (application.equals(XLD_START) && externalWorker) {
            return XLD_WITH_WORKER_START
        } else if (application.equals(XLD_STOP) && externalWorker) {
            return XLD_WITH_WORKER_STOP
        } else {
            return application
        }
    }

    static def detectApplication(application) {
        switch (application) {
            case XLD_START: return StartIntegrationServerTask.NAME
            case XLD_STOP: return ShutdownIntegrationServerTask.NAME
            case GITLAB_START: return DockerComposeGitlabStartTask.NAME
            case GITLAB_STOP: return DockerComposeGitlabStopTask.NAME
            case SATELLITE_START: return StartSatelliteTask.NAME
            case SATELLITE_STOP: return ShutdownSatelliteTask.NAME
            case CONFIG_SERVER_START: return StartConfigServerTask.NAME
            case CONFIG_SERVER_STOP: return ShutDownConfigServerTask.NAME
            case PLUGIN_MANAGER_START: return StartPluginManagerTask.NAME
            case XLD_WITH_WORKER_START: return StartWorker.NAME
            case XLD_WITH_WORKER_STOP: return [ShutdownWorker.NAME, ShutdownIntegrationServerTask.NAME]
            default: return StartIntegrationServerTask.NAME
        }
    }
}

