package ai.digital.integration.server.tasks.worker

import ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.util.ConfigurationsUtil.Companion.WORKER_DIST
import ai.digital.integration.server.util.DeployServerUtil
import ai.digital.integration.server.util.WorkerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Copy

abstract class DownloadAndExtractWorkerDistTask : DefaultTask() {

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
                        "ai.digital.deploy.task-engine:deploy-task-engine:${worker.version}@zip"
                    )

                    val taskName = "${NAME}${worker.name}"

                    this.dependsOn(project.tasks.register(taskName, Copy::class.java) { copy ->
                        copy.from(project.zipTree(project.buildscript.configurations.getByName(WORKER_DIST).singleFile))
                        copy.into(DeployServerUtil.getRelativePathInIntegrationServerDist(project, worker.name))
                    })
                }
            }
    }

    companion object {
        @JvmStatic
        val NAME = "downloadAndExtractWorkerServer"
    }
}
