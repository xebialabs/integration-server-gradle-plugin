package ai.digital.integration.server.tasks.worker

import ai.digital.integration.server.domain.Worker
import ai.digital.integration.server.util.DbUtil
import ai.digital.integration.server.util.LogbackUtil
import ai.digital.integration.server.util.WorkerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class SetWorkersLogbackLevelsTask extends DefaultTask {
    static NAME = "setWorkerLogbackLevels"

    SetWorkersLogbackLevelsTask() {
        this.configure { ->

            def slimMustRunAfter = WorkerUtil.hasSlimWorkers(project) ? [
                    CopyIntegrationServerTask.NAME
            ] : []

            def nonSlimMustRunAfter = WorkerUtil.hasNonSlimWorkers(project) ? [
                    DownloadAndExtractWorkerDistTask.NAME, SyncServerPluginsWithWorkerTask.NAME
            ] : []

            group = PLUGIN_GROUP
            mustRunAfter slimMustRunAfter + nonSlimMustRunAfter
            onlyIf {
                WorkerUtil.hasWorkers(project)
            }
        }
    }

    @TaskAction
    def setWorkersLevels() {
        WorkerUtil.getWorkers(project).forEach { worker ->
            setWorkerLevels(worker)
        }
    }

    def setWorkerLevels(Worker worker) {
        if (DbUtil.getDatabase(project).logSql || !worker.logLevels.isEmpty()) {
            if (!worker.logLevels.isEmpty() && !WorkerUtil.isExternalRuntimeWorker(project, worker)) {
                logger.warn("Log levels settings on the worker ${worker.name} are ignored because worker's runtime directory is same to the master.")
            } else {
                project.logger.lifecycle("Setting logback level on worker ${worker.name}.")
                LogbackUtil.setLogLevels(project, WorkerUtil.getWorkerWorkingDir(project, worker), worker.logLevels)
            }
        }
    }
}
