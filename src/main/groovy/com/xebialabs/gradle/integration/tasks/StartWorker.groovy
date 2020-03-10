package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.util.ExtensionsUtil
import com.xebialabs.gradle.integration.util.ProcessUtil
import com.xebialabs.gradle.integration.util.WorkerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class StartWorker extends DefaultTask {
    static NAME = "startWorker"

    StartWorker() {
        def dependencies = [
                StartIntegrationServerTask.NAME,
                StartRabbitMq.NAME
        ]

        this.configure {
            group = PLUGIN_GROUP
            dependsOn(dependencies)
            mustRunAfter(StartIntegrationServerTask.NAME)
            onlyIf {
                WorkerUtil.isWorkerEnabled(project)
            }
        }
    }

    private def getEnv() {
        def extension = ExtensionsUtil.getExtension(project)
        def opts = "-Xmx1024m"
        def suspend = extension.workerDebugSuspend ? 'y' : 'n'
        if (extension.serverDebugPort) {
            opts = "${opts} -agentlib:jdwp=transport=dt_socket,server=y,suspend=${suspend},address=${extension.serverDebugPort}"
        }
        ["DEPLOYIT_SERVER_OPTS": opts.toString()]
    }

    private def getBinDir() {
        Paths.get(ExtensionsUtil.getServerWorkingDir(project), "bin").toFile()
    }

    private void startWorker() {
        project.logger.lifecycle("Launching worker")
        def extension = ExtensionsUtil.getExtension(project)
        ProcessUtil.exec([
                command    : 'run',
                params     : [
                        "worker",
                        "-master",
                        "localhost:${extension.akkaRemotingPort}".toString(),
                        "-api",
                        "http://localhost:${extension.serverHttpPort}".toString(),
                        "-name",
                        "${extension.workerName}".toString(),
                        "-port",
                        "${extension.workerRemotingPort}".toString()
                ],
                environment: getEnv(),
                workDir    : getBinDir()
        ])
    }

    @TaskAction
    void launch() {
        startWorker()
    }
}
