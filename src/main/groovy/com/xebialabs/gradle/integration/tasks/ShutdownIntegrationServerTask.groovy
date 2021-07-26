package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.tasks.database.DockerComposeDatabaseStopTask
import com.xebialabs.gradle.integration.tasks.mq.ShutdownMq
import com.xebialabs.gradle.integration.tasks.worker.ShutdownWorker
import com.xebialabs.gradle.integration.util.DbUtil
import com.xebialabs.gradle.integration.util.ShutdownUtil
import com.xebialabs.gradle.integration.util.WorkerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class ShutdownIntegrationServerTask extends DefaultTask {
    static NAME = "shutdownIntegrationServer"

    ShutdownIntegrationServerTask() {
        group = PLUGIN_GROUP
        if (DbUtil.isDerby(project)) {
            finalizedBy("derbyStop")
        } else {
            finalizedBy(DockerComposeDatabaseStopTask.NAME)
        }
        if (WorkerUtil.isWorkerEnabled(project)) {
            dependsOn(ShutdownWorker.NAME)
            finalizedBy(ShutdownMq.NAME)
        }
    }

    @TaskAction
    void shutdown() {
        ShutdownUtil.shutdownServer(project)
    }
}
