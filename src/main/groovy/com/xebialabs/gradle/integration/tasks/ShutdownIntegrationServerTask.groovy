package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.util.DbUtil
import com.xebialabs.gradle.integration.util.ShutdownUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class ShutdownIntegrationServerTask extends DefaultTask {
    static NAME = "shutdownIntegrationServer"

    ShutdownIntegrationServerTask() {
        String finalizer
        if (DbUtil.isDerby(project)) {
            finalizer = "derbyStop"
        } else {
            finalizer = DockerComposeStopTask.NAME
        }
        this.configure {
            group = PLUGIN_GROUP
            finalizedBy(finalizer)
        }
    }

    @TaskAction
    void shutdown() {
        ShutdownUtil.shutdownServer(project)
    }
}
