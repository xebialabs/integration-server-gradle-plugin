package com.xebialabs.gradle.integration.tasks.worker

import com.xebialabs.gradle.integration.tasks.ShutdownIntegrationServerTask
import com.xebialabs.gradle.integration.tasks.mq.ShutdownMq
import com.xebialabs.gradle.integration.util.ExtensionsUtil
import com.xebialabs.gradle.integration.util.FileUtil
import com.xebialabs.gradle.integration.util.ProcessUtil
import com.xebialabs.gradle.integration.util.WorkerUtil
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.util.PluginUtil.getDistLocation
import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class ShutdownWorker extends DefaultTask {
    static NAME = "shutdownWorker"

    ShutdownWorker() {
        this.configure {
            group = PLUGIN_GROUP
            onlyIf {
                WorkerUtil.isWorkerEnabled(project)
            }
        }
    }

    private void shutdownWorker() {
        try {
            project.logger.lifecycle("Trying to shutdown workers")
            def http = new HTTPBuilder("http://localhost:${ExtensionsUtil.getExtension(project).serverHttpPort}/deployit/workers")
            http.auth.basic("admin", "admin")
            http.request( Method.DELETE ) {}
            project.logger.lifecycle("Workers shutdown successfully")
        } catch (ex) {
            project.logger.lifecycle("Could not stop workers", ex)
        }
    }


    @TaskAction
    void stop() {
        shutdownWorker()
    }
}
