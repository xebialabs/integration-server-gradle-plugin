package ai.digital.integration.server.tasks.provision

import ai.digital.integration.server.domain.DevOpsAsCode
import ai.digital.integration.server.domain.Server
import ai.digital.integration.server.tasks.StartIntegrationServerTask
import ai.digital.integration.server.tasks.worker.StartWorkersTask
import ai.digital.integration.server.util.HTTPUtil
import ai.digital.integration.server.util.ServerUtil
import ai.digital.integration.server.util.WorkerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class RunDevOpsAsCodeTask extends DefaultTask {
    public static String NAME = "runDevOpsAsCode"

    RunDevOpsAsCodeTask() {
        def dependencies = [
                StartIntegrationServerTask.NAME
        ]

        def workerDependencies = WorkerUtil.hasWorkers(project) ? [ StartWorkersTask.NAME ] : []

        this.configure {
            group = PLUGIN_GROUP
            dependsOn(dependencies + workerDependencies)
        }
    }

    private static void launchDevOpAsCodeScripts(Project project, Server server) {
        if (server.getDevOpsAsCodes() != null) {
            server.getDevOpsAsCodes().each { DevOpsAsCode devOpsAsCode ->
                def http = HTTPUtil.buildRequest("http://localhost:${server.httpPort}/deployit/devops-as-code/apply")

                http.headers.with {
                    put("Content-Type", "text/vnd.yaml")
                    put("X-Xebialabs-Scm-Author", devOpsAsCode.scmAuthor)
                    put("X-Xebialabs-Scm-Commit", devOpsAsCode.scmCommit)
                    put("X-Xebialabs-Scm-Date", devOpsAsCode.scmDate)
                    put("X-Xebialabs-Scm-Filename", devOpsAsCode.scmFile)
                    put("X-Xebialabs-Scm-Message", devOpsAsCode.scmMessage)
                    put("X-Xebialabs-Scm-Remote", devOpsAsCode.scmRemote)
                    put("X-Xebialabs-Scm-Type", devOpsAsCode.scmType)
                }
                http.post([body: devOpsAsCode.devOpAsCodeScript.toPath().text]) { resp, reader ->
                    project.logger.info("YAML ${devOpsAsCode.devOpAsCodeScript} has been applied.")
                }
            }
        }
    }

    @TaskAction
    void launch() {
        project.logger.lifecycle("Running Dev Ops as Code provision script on the Deploy server.")
        launchDevOpAsCodeScripts(project, ServerUtil.getServer(project))
    }
}
