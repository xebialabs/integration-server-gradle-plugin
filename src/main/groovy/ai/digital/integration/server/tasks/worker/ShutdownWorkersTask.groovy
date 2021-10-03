package ai.digital.integration.server.tasks.worker

import ai.digital.integration.server.tasks.mq.ShutdownMqTask
import ai.digital.integration.server.util.DeployServerUtil
import ai.digital.integration.server.util.ServerUtil
import ai.digital.integration.server.util.WorkerUtil
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class ShutdownWorkersTask extends DefaultTask {
    public static String NAME = "shutdownWorkers"

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
            def http = new HTTPBuilder("${ServerUtil.getUrl(project)}/deployit/workers")
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
