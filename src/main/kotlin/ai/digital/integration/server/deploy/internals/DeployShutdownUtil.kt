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
            var triesLeft = server.pingTotalTries

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
                triesLeft--
            }
            if (!success) {
                throw GradleException("Server failed to stop")
            }
        }

        fun killProcessByPort(project: Project, port: Int) {
            try {
                project.logger.lifecycle("Attempting to kill process on port $port")
                val osName = System.getProperty("os.name").lowercase()
                
                when {
                    osName.contains("windows") -> {
                        // Find PID using netstat
                        val findProcess = ProcessBuilder("cmd", "/c", "netstat -ano | findstr :$port")
                        findProcess.redirectErrorStream(true)
                        val findResult = findProcess.start()
                        val output = findResult.inputStream.bufferedReader().readText()
                        findResult.waitFor()
                        
                        // Extract PID from netstat output (last column)
                        val pidPattern = Regex("""LISTENING\s+(\d+)""")
                        val match = pidPattern.find(output)
                        if (match != null) {
                            val pid = match.groupValues[1]
                            project.logger.lifecycle("Found process $pid on port $port, attempting to kill")
                            val killProcess = ProcessBuilder("taskkill", "/F", "/PID", pid)
                            killProcess.redirectErrorStream(true)
                            val killResult = killProcess.start()
                            killResult.waitFor(10, TimeUnit.SECONDS)
                            project.logger.lifecycle("Forcefully killed process $pid")
                        } else {
                            project.logger.lifecycle("No process found listening on port $port")
                        }
                    }
                    osName.contains("linux") || osName.contains("mac") -> {
                        // Use lsof to find and kill the process
                        val findProcess = ProcessBuilder("sh", "-c", "lsof -ti:$port")
                        findProcess.redirectErrorStream(true)
                        val findResult = findProcess.start()
                        val pid = findResult.inputStream.bufferedReader().readText().trim()
                        findResult.waitFor()
                        
                        if (pid.isNotEmpty()) {
                            project.logger.lifecycle("Found process $pid on port $port, attempting to kill")
                            val killProcess = ProcessBuilder("kill", "-9", pid)
                            killProcess.redirectErrorStream(true)
                            val killResult = killProcess.start()
                            killResult.waitFor(10, TimeUnit.SECONDS)
                            project.logger.lifecycle("Forcefully killed process $pid")
                        } else {
                            project.logger.lifecycle("No process found listening on port $port")
                        }
                    }
                }
                
                // Give the OS time to release the port
                TimeUnit.SECONDS.sleep(2)
                
            } catch (e: Exception) {
                project.logger.warn("Failed to kill process on port $port: ${e.message}")
            }
        }

        fun shutdownServer(project: Project) {
            val server = DeployServerUtil.getServer(project)
            val port = server.httpPort
            var gracefulShutdownSucceeded = false
            
            try {
                project.logger.lifecycle("Trying to shutdown integration server on port $port")

                val client = HttpClient.newHttpClient()
                val request = HTTPUtil.doRequest(EntryPointUrlUtil(project,
                    ProductName.DEPLOY).composeUrl("/deployit/server/shutdown"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build()
                client.send(request, HttpResponse.BodyHandlers.ofString())

                waitForShutdown(project)
                project.logger.lifecycle("Integration server at port $port is now shutdown")
                gracefulShutdownSucceeded = true

            } catch (ignored: Exception) {
                project.logger.lifecycle("Integration server on port $port is not responding to graceful shutdown")
            }
            
            // If graceful shutdown failed, try to kill the process by port
            if (!gracefulShutdownSucceeded) {
                killProcessByPort(project, port)
            }
        }
    }
}
