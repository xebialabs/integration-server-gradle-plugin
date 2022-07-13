package ai.digital.integration.server.common.centralConfiguration

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.domain.CentralConfigurationServer
import ai.digital.integration.server.common.util.CentralConfigurationServerUtil
import ai.digital.integration.server.common.util.HTTPUtil
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.TimeUnit

open class ShutdownCentralConfigurationServerTask : DefaultTask() {
    companion object {
        const val NAME = "shutdownCentralConfigurationServer"
    }

    init {
        this.group = PluginConstant.PLUGIN_GROUP

        this.onlyIf {
            CentralConfigurationServerUtil.hasCentralConfigurationServer(project)
        }

        project.afterEvaluate {
        }
    }

    @TaskAction
    fun shutdown() {
        project.logger.lifecycle("About to shut down central configuration server.")
        shutdownServer(project)
    }

    private fun shutdownServer(project: Project) {
        val server = CentralConfigurationServerUtil.getCentralConfigurationServer(project)
        val port = CentralConfigurationServerUtil.readDeployitConfProperty(project, "http.port")
        try {
            project.logger.lifecycle("Trying to shutdown central configuration server on port $port")

            val client = HttpClient.newHttpClient()
            val shutdownUrl = "${CentralConfigurationServerUtil.getBaseUrl(project)}/actuator/shutdown"
            val request = HTTPUtil.doRequest(shutdownUrl, "_xl-deploy-config-admin_", "admin")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build()
            client.send(request, HttpResponse.BodyHandlers.ofString())

            waitForShutdown(project, server)
            project.logger.lifecycle("Central Configuration Server at port $port is now shutdown")

        } catch (ignored: Exception) {
            project.logger.lifecycle("Central Configuration Server on port $port is not running")
        }
    }

    private fun waitForShutdown(project: Project,
                                centralConfigurationServer: CentralConfigurationServer) {
        val triesLeft = centralConfigurationServer.pingTotalTries
        val url = CentralConfigurationServerUtil.getBaseUrl(project)
        var success = false
        while (triesLeft > 0 && !success) {
            try {
                val client = HttpClient.newHttpClient()
                val request = HTTPUtil
                        .doRequest(url)
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build()
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())

                project.logger.lifecycle("Waiting ${centralConfigurationServer.pingRetrySleepTime} seconds for shutdown. ($triesLeft)")
                TimeUnit.SECONDS.sleep(centralConfigurationServer.pingRetrySleepTime.toLong())
                if (response.statusCode() != 200 && response.statusCode() != 401) {
                    project.logger.lifecycle("Central Configuration Server successfully shutdown")
                    success = true
                }

            } catch (ignored: Exception) {
                project.logger.lifecycle("Central Configuration Server successfully shutdown.")
                success = true
                break
            }
        }
        if (!success) {
            throw GradleException("Central Configuration Server failed to stop")
        }
    }
}