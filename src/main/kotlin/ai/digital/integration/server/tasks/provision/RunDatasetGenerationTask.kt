package ai.digital.integration.server.tasks.provision

import ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.domain.Server
import ai.digital.integration.server.tasks.StartIntegrationServerTask
import ai.digital.integration.server.tasks.worker.StartWorkersTask
import ai.digital.integration.server.util.DeployServerUtil
import ai.digital.integration.server.util.HTTPUtil
import ai.digital.integration.server.util.WorkerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

abstract class RunDatasetGenerationTask : DefaultTask() {

    companion object {
        @JvmStatic
        val NAME = "runDatasetGeneration"
    }


    init {
        this.group = PLUGIN_GROUP
        this.dependsOn(StartIntegrationServerTask.NAME)

        if (WorkerUtil.hasWorkers(project)) {
            this.dependsOn(StartWorkersTask.NAME)
        }
    }

    @TaskAction
    fun launch() {
        project.logger.lifecycle("Running datasets generation on the Deploy server.")
        generateDatasets(project, DeployServerUtil.getServer(project))
    }

    private fun generateDatasets(project: Project, server: Server) {
        server.generateDatasets.forEach { dataset ->

            val client = HttpClient.newHttpClient()
            val request = HTTPUtil.doRequest(DeployServerUtil.composeUrl(project, "/deployit/generate/$dataset"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() == 200) {
                project.logger.info("DataSet ${dataset} created on Deploy server.")
            }  else {
                project.logger.error(response.body())
            }
        }
    }
}
