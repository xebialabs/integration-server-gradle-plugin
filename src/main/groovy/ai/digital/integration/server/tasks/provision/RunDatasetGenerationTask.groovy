package ai.digital.integration.server.tasks.provision

import ai.digital.integration.server.domain.Server
import ai.digital.integration.server.tasks.StartIntegrationServerTask
import ai.digital.integration.server.util.ServerUtil
import groovyx.net.http.HTTPBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class RunDatasetGenerationTask extends DefaultTask {
    static NAME = "runDatasetGeneration"

    RunDatasetGenerationTask() {
        def dependencies = [
                StartIntegrationServerTask.NAME
        ]

        this.configure {
            group = PLUGIN_GROUP
            dependsOn(dependencies)
        }
    }

    @TaskAction
    void launch() {
        project.logger.lifecycle("Running datasets generation on the Deploy server.")
        generateDatasets(ServerUtil.getServer(project))
    }

    static void generateDatasets(Server server) {
        server.generateDatasets.each { String dataset ->

            def http = new HTTPBuilder("http://localhost:${server.httpPort}/deployit/generate/${dataset}")
            http.auth.basic("admin", "admin")

            http.post([:]) { resp, reader ->
                logger.info("DataSet ${dataset} created on Deploy server.")
            }
        }
    }
}
