package ai.digital.integration.server.deploy.tasks.worker

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.deploy.domain.Worker
import ai.digital.integration.server.deploy.tasks.server.ServerYamlPatchTask
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.internals.WorkerUtil
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.nio.file.Paths

open class CopyIntegrationServerTask : DefaultTask() {

    init {
        this.group = PLUGIN_GROUP
        this.dependsOn(ServerYamlPatchTask.NAME)
        this.onlyIf {
            WorkerUtil.hasWorkers(project)
        }
    }

    @TaskAction
    fun copyServer() {
        WorkerUtil.getWorkers(project)
            .filter { worker -> !worker.slimDistribution }
            .filter { worker -> WorkerUtil.isExternalRuntimeWorker(project, worker) }
            .forEach { worker -> copyServerDirToWorkerDir(worker) }
    }

    fun copyServerDirToWorkerDir(worker: Worker) {
        val sourceDir = Paths.get(DeployServerUtil.getServerWorkingDir(project)).toFile()
        val destinationDir = Paths.get(WorkerUtil.getWorkerWorkingDir(project, worker)).toFile()

        FileUtils.copyDirectory(sourceDir, destinationDir)
        ProcessUtil.chMod(project, "755", Paths.get(destinationDir.absolutePath, "bin").toString())
        FileUtils.cleanDirectory(Paths.get(destinationDir.absolutePath, "log").toFile())
    }

    companion object {
        const val NAME = "copyIntegrationServer"
    }
}
