package ai.digital.integration.server.tasks.worker

import ai.digital.integration.server.domain.Worker
import ai.digital.integration.server.util.OverlaysUtil
import ai.digital.integration.server.util.WorkerUtil
import org.gradle.api.DefaultTask

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class WorkerOverlaysTask extends DefaultTask {
    static NAME = "workerOverlays"

    static PREFIX = "worker"

    WorkerOverlaysTask() {
        this.configure { ->

            def slimMustRunAfter = WorkerUtil.hasSlimWorkers(project) ? [
                CopyIntegrationServerTask.NAME, SetWorkersLogbackLevelsTask.NAME
            ] : []

            def nonSlimMustRunAfter = WorkerUtil.hasNonSlimWorkers(project) ? [
                DownloadAndExtractWorkerDistTask.NAME, SyncServerPluginsWithWorkerTask.NAME, SetWorkersLogbackLevelsTask.NAME
            ] : []

            group = PLUGIN_GROUP
            mustRunAfter slimMustRunAfter + nonSlimMustRunAfter
            onlyIf {
                WorkerUtil.hasWorkers(project)
            }

            project.afterEvaluate {
                WorkerUtil.getWorkers(project).each { Worker worker ->

                    if (!worker.overlays.isEmpty() && !WorkerUtil.isExternalRuntimeWorker(project, worker)) {
                        logger.warn("Overlays on the worker ${worker.name} are ignored because worker's runtime directory is same to the master.")
                    } else {
                        worker.overlays.each { Map.Entry<String, List<Object>> overlay ->
                            OverlaysUtil.defineOverlay(project, this, WorkerUtil.getWorkerWorkingDir(project, worker), PREFIX, overlay,
                                    ["downloadAndExtractWorker${worker.name}", SyncServerPluginsWithWorkerTask.NAME, SetWorkersLogbackLevelsTask.NAME])
                        }
                    }
                }
            }
        }
    }
}
