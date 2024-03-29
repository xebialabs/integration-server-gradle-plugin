package ai.digital.integration.server.deploy.internals

import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.util.HTTPUtil
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.TimeUnit

class DeployShutdownUtil {
    companion object {
        private fun waitForShutdown(project: Project) {
            val server = DeployServerUtil.getServer(project)
            val triesLeft = server.pingTotalTries

            var success = false
            while (triesLeft > 0 && !success) {
                try {
                    val client = HttpClient.newHttpClient()
                    val request = HTTPUtil
                        .doRequest(EntryPointUrlUtil(project, ProductName.DEPLOY)
                            .composeUrl(server.contextRoot))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build()
                    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

                    project.logger.lifecycle("Waiting ${server.pingRetrySleepTime} seconds for shutdown. ($triesLeft)")
                    TimeUnit.SECONDS.sleep(server.pingRetrySleepTime.toLong())

                    if (response.statusCode() != 200) {
                        project.logger.lifecycle("XL Deploy server successfully shutdown")
                        success = true
                    }

                } catch (ignored: Exception) {
                    project.logger.lifecycle("XL Deploy server successfully shutdown.")
                    success = true
                    break
                }
            }
            if (!success) {
                throw GradleException("Server failed to stop")
            }
        }

        fun shutdownServer(project: Project) {
            val server = DeployServerUtil.getServer(project)
            try {
                val port = server.httpPort
                project.logger.lifecycle("Trying to shutdown integration server on port $port")

                val client = HttpClient.newHttpClient()
                val request = HTTPUtil.doRequest(EntryPointUrlUtil(project,
                    ProductName.DEPLOY).composeUrl("/deployit/server/shutdown"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build()
                client.send(request, HttpResponse.BodyHandlers.ofString())

                waitForShutdown(project)
                project.logger.lifecycle("Integration server at port $port is now shutdown")

            } catch (ignored: Exception) {
                project.logger.lifecycle("Integration server on port ${server.httpPort} is not running")
            }
        }
    }
}
