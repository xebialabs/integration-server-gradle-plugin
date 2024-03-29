package ai.digital.integration.server.deploy.tasks.provision

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.util.HTTPUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.internals.EntryPointUrlUtil
import ai.digital.integration.server.deploy.internals.WorkerUtil
import ai.digital.integration.server.deploy.tasks.StartDeployIntegrationServerTask
import ai.digital.integration.server.deploy.tasks.worker.StartWorkersTask
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

open class RunDatasetGenerationTask : DefaultTask() {

    companion object {
        const val NAME = "runDatasetGeneration"
    }

    init {
        this.group = PLUGIN_GROUP
        this.dependsOn(StartDeployIntegrationServerTask.NAME)

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
            val request = HTTPUtil
                .doRequest(EntryPointUrlUtil(project, ProductName.DEPLOY).composeUrl("/deployit/generate/$dataset"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() == 200) {
                project.logger.info("DataSet $dataset created on Deploy server.")
            } else {
                project.logger.error(response.body())
            }
        }
    }
}
