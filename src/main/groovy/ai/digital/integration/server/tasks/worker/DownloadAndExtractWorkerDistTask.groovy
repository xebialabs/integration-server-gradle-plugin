package ai.digital.integration.server.tasks.worker

import ai.digital.integration.server.domain.Worker
import ai.digital.integration.server.util.ServerUtil
import ai.digital.integration.server.util.WorkerUtil
import org.gradle.api.Action
import org.gradle.api.tasks.Copy

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import static ai.digital.integration.server.util.ConfigurationsUtil.WORKER_DIST

class DownloadAndExtractWorkerDistTask extends Copy {
    static NAME = "downloadAndExtractWorkerServer"

    DownloadAndExtractWorkerDistTask() {
        this.configure {
            group = PLUGIN_GROUP
            onlyIf {
              WorkerUtil.hasSlimWorkers(project)
            }

            WorkerUtil.getWorkers(project)
                .findAll {Worker worker -> worker.slimDistribution}
                .each { Worker worker ->

                    if (WorkerUtil.isDistDownloadRequired(project, worker)) {
                        project.buildscript.dependencies.add(
                            WORKER_DIST,
                            "ai.digital.deploy.task-engine:deploy-task-engine:${worker.version}@zip"
                        )

                        def taskName = "downloadAndExtractWorkerServer${worker.name}"
                        def task = project.getTasks().register(taskName, Copy.class, new Action<Copy>() {
                          @Override
                          void execute(Copy copy) {
                            copy.from { project.zipTree(project.buildscript.configurations.getByName(WORKER_DIST).singleFile) }
                            copy.into { ServerUtil.getRelativePathInIntegrationServerDist(project, worker.name) }
                          }
                        })
                        this.dependsOn task
                    }
                }
        }
    }
}
