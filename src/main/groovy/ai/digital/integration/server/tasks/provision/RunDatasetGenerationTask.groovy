package ai.digital.integration.server.tasks.provision

import ai.digital.integration.server.domain.Server
import ai.digital.integration.server.tasks.StartIntegrationServerTask
import ai.digital.integration.server.tasks.worker.StartWorkersTask
import ai.digital.integration.server.util.DeployServerUtil
import ai.digital.integration.server.util.WorkerUtil
import groovyx.net.http.HTTPBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class RunDatasetGenerationTask extends DefaultTask {
    public static String NAME = "runDatasetGeneration"

    RunDatasetGenerationTask() {
        def dependencies = [
                StartIntegrationServerTask.NAME
        ]

        def workerDependencies = WorkerUtil.hasWorkers(project) ? [StartWorkersTask.NAME] : []

        this.configure {
            group = PLUGIN_GROUP
            dependsOn(dependencies + workerDependencies)
        }
    }

    @TaskAction
    void launch() {
        project.logger.lifecycle("Running datasets generation on the Deploy server.")
        generateDatasets(project, DeployServerUtil.getServer(project))
    }

    static void generateDatasets(Project project, Server server) {
        server.generateDatasets.each { String dataset ->

            def http = new HTTPBuilder("${DeployServerUtil.getUrl(project)}/deployit/generate/${dataset}")
            http.auth.basic("admin", "admin")

            http.post([:]) { resp, reader ->
                project.logger.info("DataSet ${dataset} created on Deploy server.")
            }
        }
    }
}
