package ai.digital.integration.server.deploy.tasks.worker

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.ServerUtil
import ai.digital.integration.server.deploy.util.DeployConfigurationsUtil.Companion.WORKER_DIST
import ai.digital.integration.server.deploy.util.WorkerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Copy

open class DownloadAndExtractWorkerDistTask : DefaultTask() {

    init {
        this.group = PLUGIN_GROUP
        this.onlyIf {
            WorkerUtil.hasSlimWorkers(project)
        }

        WorkerUtil.getWorkers(project)
            .filter { worker -> worker.slimDistribution }
            .forEach { worker ->
                if (WorkerUtil.isDistDownloadRequired(project, worker)) {
                    project.buildscript.dependencies.add(
                        WORKER_DIST,
                        "ai.digital.deploy.task-engine:deploy-task-engine-base:${worker.version}@zip"
                    )

                    val taskName = "$NAME${worker.name}"

                    this.dependsOn(project.tasks.register(taskName, Copy::class.java) { copy ->
                        copy.from(project.zipTree(project.buildscript.configurations.getByName(WORKER_DIST).singleFile))
                        copy.into(ServerUtil.getRelativePathInIntegrationServerDist(project, worker.name))
                    })
                }
            }
    }

    companion object {
        const val NAME = "downloadAndExtractWorkerServer"
    }
}
