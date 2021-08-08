package com.xebialabs.gradle.integration.tasks.worker

import com.xebialabs.gradle.integration.tasks.mq.ShutdownMqTask
import com.xebialabs.gradle.integration.util.ServerUtil
import com.xebialabs.gradle.integration.util.WorkerUtil
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.constant.PluginConstant.PLUGIN_GROUP

class ShutdownWorkersTask extends DefaultTask {
    static NAME = "shutdownWorkers"

    ShutdownWorkersTask() {
        def dependencies = [
                ShutdownMqTask.NAME
        ]
        this.configure {
            dependsOn(dependencies)
            group = PLUGIN_GROUP
            onlyIf {
                WorkerUtil.hasWorkers(project)
            }
        }
    }

    private void shutdownWorkers() {
        try {
            project.logger.lifecycle("About to shutdown all workers")
            def http = new HTTPBuilder("http://localhost:${ServerUtil.getServer(project).httpPort}/deployit/workers")
            http.auth.basic("admin", "admin")
            http.request(Method.DELETE) {}
            project.logger.lifecycle("Workers shutdown successfully")
        } catch (ignore) {
        }
    }

    @TaskAction
    void stop() {
        shutdownWorkers()
    }
}
