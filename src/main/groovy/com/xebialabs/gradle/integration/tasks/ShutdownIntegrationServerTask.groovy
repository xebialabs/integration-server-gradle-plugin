package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.tasks.database.DockerComposeDatabaseStopTask
import com.xebialabs.gradle.integration.tasks.worker.ShutdownWorkers
import com.xebialabs.gradle.integration.util.DbUtil
import com.xebialabs.gradle.integration.util.ShutdownUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class ShutdownIntegrationServerTask extends DefaultTask {
    static NAME = "shutdownIntegrationServer"

    ShutdownIntegrationServerTask() {
        def dependencies = [
                ShutdownWorkers.NAME
        ]
        group = PLUGIN_GROUP
        if (DbUtil.isDerby(project)) {
            finalizedBy("derbyStop")
        } else {
            finalizedBy(DockerComposeDatabaseStopTask.NAME)
        }
        this.configure {
            group = PLUGIN_GROUP
            dependsOn(dependencies)
        }
    }

    @TaskAction
    void shutdown() {
        ShutdownUtil.shutdownServer(project)
    }
}
