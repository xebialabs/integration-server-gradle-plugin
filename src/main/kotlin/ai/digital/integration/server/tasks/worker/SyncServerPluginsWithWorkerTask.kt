package ai.digital.integration.server.tasks.worker

import ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.domain.Worker
import ai.digital.integration.server.util.DeployServerUtil
import ai.digital.integration.server.util.FileUtil
import ai.digital.integration.server.util.ProcessUtil
import ai.digital.integration.server.util.WorkerUtil
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.nio.file.Paths

abstract class SyncServerPluginsWithWorkerTask : DefaultTask() {

    init {
        this.group = PLUGIN_GROUP
        this.dependsOn(DownloadAndExtractWorkerDistTask.NAME)
        this.onlyIf {
            WorkerUtil.hasWorkers(project)
        }
    }

    @TaskAction
    fun copyToWorkers() {
        WorkerUtil.getWorkers(project)
            .filter { worker -> worker.slimDistribution }
            .filter { worker -> WorkerUtil.isExternalRuntimeWorker(project, worker) }
            .forEach { worker ->
                copyServerDirToWorkerDir(worker)
            }
    }

    private fun copyServerDirToWorkerDir(worker: Worker) {
        val sourceDir = DeployServerUtil.getServerWorkingDir(project)
        val destinationDir = Paths.get(WorkerUtil.getWorkerWorkingDir(project, worker)).toFile()
        ProcessUtil.chMod(
            project,
            "755",
            "${Paths.get(WorkerUtil.getWorkerWorkingDir(project, worker), "bin").toAbsolutePath()}"
        )

        project.logger.lifecycle("Copy plugins from directory ${sourceDir} to Worker ${worker.name} in directory ${destinationDir}")

        // delete plugins from zip
        FileUtils.deleteDirectory(Paths.get(WorkerUtil.getWorkerWorkingDir(project, worker), "plugins").toFile())

        FileUtil.copyDirs(
            DeployServerUtil.getServerWorkingDir(project), WorkerUtil.getWorkerWorkingDir(project, worker), listOf(
                "ext",
                "hotfix",
                "plugins"
            )
        )

        FileUtil.copyFiles(
            Paths.get(DeployServerUtil.getServerWorkingDir(project), "conf").toAbsolutePath().toString(),
            Paths.get(WorkerUtil.getWorkerWorkingDir(project, worker), "conf").toAbsolutePath().toString(),
            listOf(
                "deployit-license.lic"
            )
        )
    }

    companion object {
        @JvmStatic
        val NAME = "syncServerPluginsWithWorker"
    }
}
