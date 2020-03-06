package com.xebialabs.gradle.integration.tasks.satellite

import com.xebialabs.gradle.integration.util.ShutdownUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class StopSatelliteTask extends DefaultTask {
    static NAME = "stopSatellite"

    StopSatelliteTask() {
        group = PLUGIN_GROUP

    }

    @TaskAction
    void stop() {
        ShutdownUtil.shutdownServer(project)
    }
}
