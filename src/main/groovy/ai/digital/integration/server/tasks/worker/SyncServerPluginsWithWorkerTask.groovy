package ai.digital.integration.server.tasks.worker

import ai.digital.integration.server.domain.Worker
import ai.digital.integration.server.util.ProcessUtil
import ai.digital.integration.server.util.ServerUtil
import ai.digital.integration.server.util.WorkerUtil
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class SyncServerPluginsWithWorkerTask extends DefaultTask {
    static NAME = "syncServerPluginsWithWorker"

    def dependencies = [
            DownloadAndExtractWorkerDistTask.NAME
    ]

    SyncServerPluginsWithWorkerTask() {
        this.configure { ->
            group = PLUGIN_GROUP
            dependsOn(dependencies)
        }
    }

    @TaskAction
    def copyToWorkers() {
        WorkerUtil.getWorkers(project).forEach { Worker worker ->
            copyServerDirToWorkerDir(worker)
        }
    }

    void copyServerDirToWorkerDir(Worker worker) {
        if (!WorkerUtil.isExternalRuntimeWorker(worker, project)) {
            def sourceDir = ServerUtil.getServerWorkingDir(project)
            def destinationDir = Paths.get(WorkerUtil.getWorkerWorkingDir(worker, project)).toFile()
            destinationDir.setExecutable(true)
            ProcessUtil.chMod(project, "755", "${destinationDir.getAbsolutePath().toString()}")

            project.logger.lifecycle("Copy Worker ${worker.name} runtime from ${sourceDir} to ${destinationDir}")

            // delete plugins from zip
            FileUtils.deleteDirectory(Paths.get(WorkerUtil.getWorkerWorkingDir(worker, project), "plugins").toFile())

            [
                    "hotfix",
                    "plugins"
            ].forEach { String dir ->
                FileUtils.copyDirectory(
                        Paths.get(ServerUtil.getServerWorkingDir(project), dir).toFile(),
                        Paths.get(WorkerUtil.getWorkerWorkingDir(worker, project), dir).toFile()
                )
            }

            [
                    "deployit-license.lic"
            ].forEach { confFile ->
                FileUtils.copyFileToDirectory(
                        Paths.get(ServerUtil.getServerWorkingDir(project), "conf", confFile).toFile(),
                        Paths.get(WorkerUtil.getWorkerWorkingDir(worker, project), "conf").toFile()
                )
            }
        }
    }

}
