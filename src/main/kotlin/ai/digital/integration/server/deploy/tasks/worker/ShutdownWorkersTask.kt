package ai.digital.integration.server.deploy.tasks.worker

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.mq.ShutdownMqTask
import ai.digital.integration.server.deploy.util.DeployServerUtil
import ai.digital.integration.server.common.util.HTTPUtil
import ai.digital.integration.server.deploy.util.WorkerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.net.http.HttpClient
import java.net.http.HttpResponse

abstract class ShutdownWorkersTask : DefaultTask() {

    companion object {
        const val NAME = "shutdownWorkers"
    }

    init {
        this.dependsOn(ShutdownMqTask.NAME)
        this.group = PLUGIN_GROUP
        this.onlyIf {
            WorkerUtil.hasWorkers(project)
        }

    }

    private fun shutdownWorkers() {
        try {
            project.logger.lifecycle("About to shutdown all workers")

            val client = HttpClient.newHttpClient()
            val request = HTTPUtil.doRequest(DeployServerUtil.composeUrl(project, "/deployit/workers"))
                .DELETE()
                .build()

            client.send(request, HttpResponse.BodyHandlers.ofString())
            project.logger.lifecycle("Workers shutdown successfully")
        } catch (ignore: Exception) {
        }
    }

    @TaskAction
    fun stop() {
        shutdownWorkers()
    }
}
