package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.tasks.database.DatabaseStopTask
import com.xebialabs.gradle.integration.tasks.worker.ShutdownWorkers
import com.xebialabs.gradle.integration.util.DbUtil
import com.xebialabs.gradle.integration.util.ShutdownUtil
import com.xebialabs.gradle.integration.util.WorkerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.constant.PluginConstant.PLUGIN_GROUP

class ShutdownIntegrationServerTask extends DefaultTask {
    static NAME = "shutdownIntegrationServer"

    ShutdownIntegrationServerTask() {
        def dependencies = []
        if (WorkerUtil.hasWorkers(project)) {
            dependencies.push(ShutdownWorkers.NAME)
        }
        group = PLUGIN_GROUP
        if (DbUtil.isDerby(project)) {
            finalizedBy("derbyStop")
        } else {
            finalizedBy(DatabaseStopTask.NAME)
        }
        this.configure {
            group = PLUGIN_GROUP
            if (!dependencies.empty) {
                dependsOn(dependencies)
            }
        }
    }

    @TaskAction
    void shutdown() {
        project.logger.lifecycle("About to shutting down Deploy Server.")
        ShutdownUtil.shutdownServer(project)
    }
}
