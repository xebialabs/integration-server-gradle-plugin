package ai.digital.integration.server.deploy.tasks.worker

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.mq.ShutdownMqTask
import ai.digital.integration.server.common.util.HTTPUtil
import ai.digital.integration.server.deploy.internals.EntryPointUrlUtil
import ai.digital.integration.server.deploy.internals.WorkerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.net.http.HttpClient
import java.net.http.HttpResponse
import java.util.concurrent.TimeUnit

open class ShutdownWorkersTask : DefaultTask() {

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

    private fun killWorkerProcessByPort(port: String) {
        try {
            project.logger.lifecycle("Attempting to kill worker process on port $port")
            val osName = System.getProperty("os.name").lowercase()
            
            when {
                osName.contains("windows") -> {
                    val findProcess = ProcessBuilder("cmd", "/c", "netstat -ano | findstr :$port")
                    findProcess.redirectErrorStream(true)
                    val findResult = findProcess.start()
                    val output = findResult.inputStream.bufferedReader().readText()
                    findResult.waitFor()
                    
                    val pidPattern = Regex("""LISTENING\s+(\d+)""")
                    val match = pidPattern.find(output)
                    if (match != null) {
                        val pid = match.groupValues[1]
                        project.logger.lifecycle("Found worker process $pid on port $port, attempting to kill")
                        val killProcess = ProcessBuilder("taskkill", "/F", "/PID", pid)
                        killProcess.redirectErrorStream(true)
                        val killResult = killProcess.start()
                        killResult.waitFor(10, TimeUnit.SECONDS)
                        project.logger.lifecycle("Forcefully killed worker process $pid")
                    }
                }
                osName.contains("linux") || osName.contains("mac") -> {
                    val findProcess = ProcessBuilder("sh", "-c", "lsof -ti:$port")
                    findProcess.redirectErrorStream(true)
                    val findResult = findProcess.start()
                    val pid = findResult.inputStream.bufferedReader().readText().trim()
                    findResult.waitFor()
                    
                    if (pid.isNotEmpty()) {
                        project.logger.lifecycle("Found worker process $pid on port $port, attempting to kill")
                        val killProcess = ProcessBuilder("kill", "-9", pid)
                        killProcess.redirectErrorStream(true)
                        val killResult = killProcess.start()
                        killResult.waitFor(10, TimeUnit.SECONDS)
                        project.logger.lifecycle("Forcefully killed worker process $pid")
                    }
                }
            }
            
            TimeUnit.SECONDS.sleep(2)
            
        } catch (e: Exception) {
            project.logger.warn("Failed to kill worker process on port $port: ${e.message}")
        }
    }

    private fun shutdownWorkers() {
        var gracefulShutdownSucceeded = false
        try {
            project.logger.lifecycle("About to shutdown all workers")
            val client = HttpClient.newHttpClient()
            val request =
                HTTPUtil.doRequest(EntryPointUrlUtil(project, ProductName.DEPLOY).composeUrl("/deployit/workers"))
                    .DELETE()
                    .build()

            client.send(request, HttpResponse.BodyHandlers.ofString())
            project.logger.lifecycle("Workers shutdown successfully")
            gracefulShutdownSucceeded = true
        } catch (ignore: Exception) {
            project.logger.lifecycle("Workers did not respond to graceful shutdown")
        }
        
        // If graceful shutdown failed, try to kill worker processes by port
        if (!gracefulShutdownSucceeded) {
            WorkerUtil.getWorkers(project).forEach { worker ->
                killWorkerProcessByPort(worker.port)
            }
        }
    }

    @TaskAction
    fun stop() {
        shutdownWorkers()
    }
}
