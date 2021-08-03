package com.xebialabs.gradle.integration.tasks.worker

import com.xebialabs.gradle.integration.tasks.StartIntegrationServerTask
import com.xebialabs.gradle.integration.tasks.database.ImportDbUnitDataTask
import com.xebialabs.gradle.integration.tasks.mq.StartMq
import com.xebialabs.gradle.integration.util.ExtensionsUtil
import com.xebialabs.gradle.integration.util.ProcessUtil
import com.xebialabs.gradle.integration.util.WorkerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class CopyWorkerTask extends DefaultTask {
    static NAME = "copyWorker"

    CopyWorkerTask() {
        def dependencies = [
                StartIntegrationServerTask.NAME,
                StartMq.NAME
        ]
        this.configure {
            group = PLUGIN_GROUP
            dependsOn(dependencies)
            shouldRunAfter(StartIntegrationServerTask.NAME, ImportDbUnitDataTask.NAME, StartMq.NAME)
            onlyIf {
                !WorkerUtil.isWorkerDirLocal(project)
            }
        }
    }

    @TaskAction
    void copyServerDirToWorkerDir() {
            def source = ExtensionsUtil.getServerWorkingDir(project)
            def target = WorkerUtil.getWorkerRunTimeDirectory(project)
            ProcessUtil.cpServerDirToWorkDir(project, source, target)
    }
}
