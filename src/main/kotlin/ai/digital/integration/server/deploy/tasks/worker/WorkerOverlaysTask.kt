package ai.digital.integration.server.deploy.tasks.worker

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.OverlaysUtil
import ai.digital.integration.server.deploy.internals.WorkerUtil
import org.gradle.api.DefaultTask

open class WorkerOverlaysTask : DefaultTask() {

    companion object {
        const val NAME = "workerOverlays"
        const val PREFIX = "worker"
    }

    init {

        if (WorkerUtil.hasSlimWorkers(project)) {
            this.dependsOn(DownloadAndExtractWorkerDistTask.NAME)
            this.dependsOn(SyncServerPluginsWithWorkerTask.NAME)
            this.dependsOn(SetWorkersLogbackLevelsTask.NAME)

            this.mustRunAfter(DownloadAndExtractWorkerDistTask.NAME)
            this.mustRunAfter(SyncServerPluginsWithWorkerTask.NAME)
            this.mustRunAfter(SetWorkersLogbackLevelsTask.NAME)
        }

        if (WorkerUtil.hasNonSlimWorkers(project)) {
            this.dependsOn(CopyIntegrationServerTask.NAME)
            this.dependsOn(SetWorkersLogbackLevelsTask.NAME)

            this.mustRunAfter(CopyIntegrationServerTask.NAME)
            this.mustRunAfter(SetWorkersLogbackLevelsTask.NAME)
        }


        this.group = PLUGIN_GROUP
        this.onlyIf {
            WorkerUtil.hasWorkers(project)
        }

        project.afterEvaluate {
            WorkerUtil.getWorkers(project).forEach { worker ->

                if (worker.slimDistribution) {
                    OverlaysUtil.addDatabaseDependency(project, worker)
                    OverlaysUtil.addMqDependency(project, worker)
                }

                if (worker.overlays.isNotEmpty() && !WorkerUtil.isExternalRuntimeWorker(project, worker)) {
                    logger.warn("Overlays on the worker ${worker.name} are ignored because worker's runtime directory is same to the master.")
                } else {
                    worker.overlays.forEach { overlay ->
                        OverlaysUtil.defineOverlay(project,
                            this,
                            WorkerUtil.getWorkerWorkingDir(project, worker),
                            PREFIX,
                            overlay,
                            arrayListOf(
                                "${DownloadAndExtractWorkerDistTask.NAME}${worker.name}",
                                SyncServerPluginsWithWorkerTask.NAME,
                                SetWorkersLogbackLevelsTask.NAME
                            )
                        )
                    }
                }
            }
        }
    }
}
