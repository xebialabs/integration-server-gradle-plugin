package com.xebialabs.gradle.integration.tasks.worker

import com.xebialabs.gradle.integration.tasks.ShutdownIntegrationServerTask
import com.xebialabs.gradle.integration.tasks.mq.ShutdownMq
import com.xebialabs.gradle.integration.util.FileUtil
import com.xebialabs.gradle.integration.util.ProcessUtil
import com.xebialabs.gradle.integration.util.WorkerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.util.PluginUtil.getDistLocation
import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class ShutdownWorker extends DefaultTask {
    static NAME = "shutdownWorker"
    static SHUTDOWN_WORKER_SCRIPT = "shutdownWorker.sh"

    ShutdownWorker() {
        def dependencies = [
                ShutdownIntegrationServerTask.NAME,
                ShutdownMq.NAME
        ]

        this.configure {
            group = PLUGIN_GROUP
            dependsOn(dependencies)
            shouldRunAfter(ShutdownIntegrationServerTask.NAME, ShutdownMq.NAME)
            onlyIf {
                WorkerUtil.isWorkerEnabled(project)
            }
        }
    }

    private def copyShutdownWorkerScript() {
        def from = ShutdownWorker.class.classLoader.getResourceAsStream("worker/bin/$SHUTDOWN_WORKER_SCRIPT")
        def intoDir = getDistLocation(project).resolve(SHUTDOWN_WORKER_SCRIPT)
        FileUtil.copyFile(from, intoDir)
    }

    private def getWorkingDir() {
        return getDistLocation(project).toFile()
    }

    private void shutdownWorker() {
        project.logger.lifecycle("Shutdown Worker")

        ProcessUtil.chMod(project, "777", getDistLocation(project).resolve(SHUTDOWN_WORKER_SCRIPT).toAbsolutePath().toString())
        ProcessUtil.exec([
                command: "shutdownWorker",
                workDir: getWorkingDir()
        ])
        project.logger.info("Worker successfully shutdown.")
    }


    @TaskAction
    void stop() {
        copyShutdownWorkerScript()
        shutdownWorker()
    }
}
