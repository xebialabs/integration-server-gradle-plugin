package ai.digital.integration.server.tasks.worker

import ai.digital.integration.server.domain.Worker
import ai.digital.integration.server.tasks.YamlPatchTask
import ai.digital.integration.server.util.DeployServerUtil
import ai.digital.integration.server.util.ProcessUtil
import ai.digital.integration.server.util.ServerUtil
import ai.digital.integration.server.util.WorkerUtil
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class CopyIntegrationServerTask extends DefaultTask {
    public static String NAME = "copyIntegrationServer"

    def dependencies = [
        YamlPatchTask.NAME
    ]

    CopyIntegrationServerTask() {
        this.configure { ->
            group = PLUGIN_GROUP
            dependsOn(dependencies)
            onlyIf {
                WorkerUtil.hasWorkers(project)
            }
        }
    }

    @TaskAction
    def copyServer() {
        WorkerUtil.getWorkers(project)
            .findAll {worker -> !worker.slimDistribution }
            .findAll {worker -> WorkerUtil.isExternalRuntimeWorker(project, worker) }
            .forEach { worker -> copyServerDirToWorkerDir(worker) }
    }

    void copyServerDirToWorkerDir(Worker worker) {
        def sourceDir = Paths.get(DeployServerUtil.getServerWorkingDir(project)).toFile()
        def destinationDir = Paths.get(WorkerUtil.getWorkerWorkingDir(project, worker)).toFile()

        FileUtils.copyDirectory(sourceDir, destinationDir)
        ProcessUtil.chMod(project, "755", Paths.get(destinationDir.getAbsolutePath(), "bin").toString())
        // delete log dir
        FileUtils.cleanDirectory(Paths.get(destinationDir.getAbsolutePath(), "log").toFile())
    }
}
