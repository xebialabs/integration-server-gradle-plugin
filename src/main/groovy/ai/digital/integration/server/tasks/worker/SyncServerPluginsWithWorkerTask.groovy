package ai.digital.integration.server.tasks.worker

import ai.digital.integration.server.domain.Worker
import ai.digital.integration.server.util.FileUtil
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
            onlyIf {
                WorkerUtil.hasWorkers(project)
            }
        }
    }

    @TaskAction
    def copyToWorkers() {
        WorkerUtil.getWorkers(project)
            .findAll {worker -> worker.slimDistribution}
            .findAll {worker -> !WorkerUtil.isExternalRuntimeWorker(project, worker)}
            .forEach { worker ->
                    copyServerDirToWorkerDir(worker)
            }
    }

    void copyServerDirToWorkerDir(Worker worker) {
        def sourceDir = ServerUtil.getServerWorkingDir(project)
        def destinationDir = Paths.get(WorkerUtil.getWorkerWorkingDir(project, worker)).toFile()
        ProcessUtil.chMod(project, "755", "${Paths.get(WorkerUtil.getWorkerWorkingDir(project, worker), "bin").toAbsolutePath().toString()}")

        project.logger.lifecycle("Copy plugins from directory ${sourceDir} to Worker ${worker.name} in directory ${destinationDir}")

        // delete plugins from zip
        FileUtils.deleteDirectory(Paths.get(WorkerUtil.getWorkerWorkingDir(project, worker), "plugins").toFile())

        FileUtil.copyDirs(ServerUtil.getServerWorkingDir(project), WorkerUtil.getWorkerWorkingDir(project, worker), [
            "hotfix",
            "plugins"
        ])

        FileUtil.copyFiles(
            Paths.get(ServerUtil.getServerWorkingDir(project), "conf").toAbsolutePath().toString(),
            Paths.get(WorkerUtil.getWorkerWorkingDir(project, worker), "conf").toAbsolutePath().toString(),
            [
                "deployit-license.lic"
            ]
        )
    }
}
