package com.xebialabs.gradle.integration.tasks.worker

import com.xebialabs.gradle.integration.tasks.mq.ShutdownMq
import com.xebialabs.gradle.integration.util.ExtensionsUtil
import com.xebialabs.gradle.integration.util.WorkerUtil
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class ShutdownWorkers extends DefaultTask {
    static NAME = "shutdownWorkers"

    ShutdownWorkers() {
        def dependencies = [
                ShutdownMq.NAME
        ]
        this.configure {
            dependsOn(dependencies)
            group = PLUGIN_GROUP
            onlyIf {
                WorkerUtil.hasWorkers(project)
            }
        }
    }

    private void shutdownWorker() {
        try {
            project.logger.lifecycle("Trying to shutdown workers")
            def http = new HTTPBuilder("http://localhost:${ExtensionsUtil.getExtension(project).serverHttpPort}/deployit/workers")
            http.auth.basic("admin", "admin")
            http.request(Method.DELETE) {}
            project.logger.lifecycle("Workers shutdown successfully")
        } catch (ignore) {
        }
    }


    @TaskAction
    void stop() {
        shutdownWorker()
    }
}
