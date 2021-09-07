package ai.digital.integration.server.tasks.worker

import ai.digital.integration.server.domain.Worker
import ai.digital.integration.server.util.WorkerUtil
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Copy

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class WorkerOverlaysTask extends DefaultTask {
    static NAME = "workerOverlays"

    static PREFIX = "worker"

    static boolean shouldUnzip(File file) {
        file.name.endsWith(".zip")
    }

    WorkerOverlaysTask() {
        this.configure { ->
            group = PLUGIN_GROUP
            mustRunAfter DownloadAndExtractWorkerDistTask.NAME, SyncServerPluginsWithWorkerTask.NAME, SetWorkersLogbackLevelsTask.NAME

            project.afterEvaluate {
                WorkerUtil.getWorkers(project).each { Worker worker ->

                    if (!worker.overlays.isEmpty() && !WorkerUtil.isExternalRuntimeWorker(worker, project)) {
                        logger.warn("Overlays on the worker ${worker.name} are ignored because worker's runtime directory is same to the master.")
                    } else {
                        worker.overlays.each { Map.Entry<String, List<Object>> overlay ->
                            def configurationName = "${PREFIX}${overlay.key.capitalize().replace("/", "")}"
                            def config = project.buildscript.configurations.create(configurationName)
                            overlay.value.each { dependencyNotation ->
                                project.buildscript.dependencies.add(configurationName, dependencyNotation)
                            }

                            def task = project.getTasks().register("copy${configurationName.capitalize()}", Copy.class, new Action<Copy>() {
                                @Override
                                void execute(Copy copy) {
                                    config.files.each { file ->
                                        copy.from { shouldUnzip(file) ? project.zipTree(file) : file }
                                    }
                                    copy.into { "${WorkerUtil.getWorkerWorkingDir(worker, project)}/${overlay.key}" }
                                }
                            })
                            project.tasks.getByName(task.name).dependsOn "downloadAndExtractWorker${worker.name}", SyncServerPluginsWithWorkerTask.NAME, SetWorkersLogbackLevelsTask.NAME
                            this.dependsOn task
                        }
                    }
                }
            }
        }
    }
}
