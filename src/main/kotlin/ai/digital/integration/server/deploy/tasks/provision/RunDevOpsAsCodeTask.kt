package ai.digital.integration.server.deploy.tasks.provision

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.DevOpsAsCode
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.util.HTTPUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.internals.EntryPointUrlUtil
import ai.digital.integration.server.deploy.internals.WorkerUtil
import ai.digital.integration.server.deploy.tasks.server.StartServerInstanceTask
import ai.digital.integration.server.deploy.tasks.worker.StartWorkersTask
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

open class RunDevOpsAsCodeTask : DefaultTask() {

    companion object {
        const val NAME = "runDevOpsAsCode"
    }

    init {
        this.group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(StartServerInstanceTask.NAME)

        if (WorkerUtil.hasWorkers(project)) {
            this.dependsOn(StartWorkersTask.NAME)
        }
    }

    private fun launchDevOpAsCodeScripts(project: Project, server: Server) {
        if (server.devOpsAsCodes != null) {
            server.devOpsAsCodes?.forEach { devOpsAsCode: DevOpsAsCode ->
                devOpsAsCode.devOpAsCodeScript?.let { script ->
                    val client = HttpClient.newHttpClient()

                    val headers = mutableListOf<String>()

                    fun addHeaders(key: String, value: String?) {
                        if (value != null) {
                            headers.addAll(arrayListOf(key, value))
                        }
                    }

                    addHeaders("X-Xebialabs-Scm-Author", devOpsAsCode.scmAuthor)
                    addHeaders("X-Xebialabs-Scm-Commit", devOpsAsCode.scmCommit)
                    addHeaders("X-Xebialabs-Scm-Date", devOpsAsCode.scmDate)
                    addHeaders("X-Xebialabs-Scm-Filename", devOpsAsCode.scmFile)
                    addHeaders("X-Xebialabs-Scm-Message", devOpsAsCode.scmMessage)
                    addHeaders("X-Xebialabs-Scm-Remote", devOpsAsCode.scmRemote)
                    addHeaders("X-Xebialabs-Scm-Type", devOpsAsCode.scmType)

                    val request =
                        HTTPUtil.doRequest(EntryPointUrlUtil(project,
                            ProductName.DEPLOY).composeUrl("/deployit/devops-as-code/apply"))
                            .headers(
                                "Content-Type", "text/vnd.yaml",
                                *headers.toTypedArray()
                            )
                            .POST(HttpRequest.BodyPublishers.ofString(script.toPath().toFile()
                                .readText(Charsets.UTF_8)))
                            .build()
                    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                    if (response.statusCode() == 200) {
                        project.logger.info("YAML ${devOpsAsCode.devOpAsCodeScript} has been applied.")
                    } else {
                        project.logger.error(response.body())
                    }
                }
            }
        }
    }

    @TaskAction
    fun launch() {
        project.logger.lifecycle("Running Dev Ops as Code provision script on the Deploy server.")
        launchDevOpAsCodeScripts(project, DeployServerUtil.getServer(project))
    }
}
