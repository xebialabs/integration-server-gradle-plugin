package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.Worker
import com.xebialabs.gradle.integration.tasks.database.ImportDbUnitDataTask
import com.xebialabs.gradle.integration.tasks.mq.StartMq
import com.xebialabs.gradle.integration.tasks.worker.StartWorker
import com.xebialabs.gradle.integration.util.ExtensionsUtil
import com.xebialabs.gradle.integration.util.WorkerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class WorkersTask extends DefaultTask {
    static NAME = "workers"

    WorkersTask() {
        def dependencies = [
                StartIntegrationServerTask.NAME,
                StartMq.NAME
        ]
        this.configure {
            group = PLUGIN_GROUP
            dependsOn(dependencies)
            shouldRunAfter(StartIntegrationServerTask.NAME, ImportDbUnitDataTask.NAME, StartMq.NAME)
            onlyIf {
                WorkerUtil.isWorkerEnabled(project)
            }
        }
    }

    private static def resolveValue(Project project, Worker worker, String propertyName, def defaultValue) {
        if (project.hasProperty(propertyName)) {
            project.property(propertyName)
        } else {
            def propertyValue = worker[propertyName]
            propertyValue ? propertyValue : defaultValue
        }
    }

    private static def resolveIntValue(Project project, Worker worker, String propertyName, def defaultValue) {
        def value = resolveValue(project, worker, propertyName, defaultValue)
        if (value == null) {
            null as Integer
        } else Integer.parseInt(value as String)
    }

    private static def resolveBooleanValue(Project project, Worker worker, String propertyName, Boolean defaultValue) {
        if (project.hasProperty(propertyName)) {
            def value = project.property(propertyName)
            !value || Boolean.parseBoolean(value as String)
        } else {
            def valueFromExtension = worker[propertyName]
            valueFromExtension != null ? valueFromExtension : defaultValue
        }
    }

    @TaskAction
    def workers() {
        project.logger.lifecycle("creating the Workers")
        def workersList = ExtensionsUtil.getExtension(project).workers

        if (workersList && workersList.size() > 0) {
            workersList.eachWithIndex { worker, index ->
                def task = project.getTasks().register("startWorker-${index}", StartWorker.class) {
                    port = resolveIntValue(project, worker, "workerRemotingPort", ExtensionsUtil.findFreePort())
                    name = resolveValue(project, worker, "workerName", "worker-1-work")
                    debugPort = resolveIntValue(project, worker, "workerDebugPort", null)
                    jvmArgs = resolveValue(project, worker, "workerJvmArgs", ["-Xmx1024m", "-Duser.timezone=UTC"])
                    directoryLocal = resolveBooleanValue(project, worker, "workerDirLocal", true)
                    directory = resolveValue(project, worker, "workerRuntimeDirectory", WorkerUtil.getExternalWorkerDir(project))
                }
                //project.tasks.getByName(task.name).dependsOn DownloadAndExtractServerDistTask.NAME
                this.dependsOn task
            }
        }
    }
}
