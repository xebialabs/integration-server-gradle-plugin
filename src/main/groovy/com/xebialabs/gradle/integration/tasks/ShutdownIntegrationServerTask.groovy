package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.util.ShutdownUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class ShutdownIntegrationServerTask extends DefaultTask {
    static NAME = "shutdownIntegrationServer"

    ShutdownIntegrationServerTask() {
        this.configure {
            group = PLUGIN_GROUP
            finalizedBy("derbyStop")
        }
    }

    @TaskAction
    void shutdown() {
        ShutdownUtil.shutdownServer(project)
    }
}
