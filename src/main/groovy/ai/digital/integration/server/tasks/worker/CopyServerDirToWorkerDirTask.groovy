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

class CopyServerDirToWorkerDirTask extends DefaultTask {
    static NAME = "copyServerDirToWorkerDir"

    CopyServerDirToWorkerDirTask() {
        this.configure { ->
            group = PLUGIN_GROUP
            mustRunAfter DownloadAndExtractWorkerDistTask.NAME
        }
    }

    @TaskAction
    def copyToWorkers() {
        WorkerUtil.getWorkers(project).forEach { worker ->
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

            def dirs = [
                "hotfix",
                "plugins",
                "importablePackages",
            ]
            dirs.forEach { dir ->
                FileUtils.copyDirectory(
                    Paths.get(ServerUtil.getServerWorkingDir(project), dir).toFile(),
                    Paths.get(WorkerUtil.getWorkerWorkingDir(worker, project), dir).toFile()
                )
            }

            def confFiles = [
                "deployit.conf",
                "deployit-license.lic"
            ]
            confFiles.forEach { confFile ->
                FileUtils.copyFileToDirectory(
                    Paths.get(ServerUtil.getServerWorkingDir(project), "conf", confFile).toFile(),
                    Paths.get(WorkerUtil.getWorkerWorkingDir(worker, project), "conf").toFile()
                )
            }
        }
    }

}
