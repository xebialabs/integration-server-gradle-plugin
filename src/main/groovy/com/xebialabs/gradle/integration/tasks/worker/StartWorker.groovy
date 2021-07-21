package com.xebialabs.gradle.integration.tasks.worker

import com.xebialabs.gradle.integration.tasks.StartIntegrationServerTask
import com.xebialabs.gradle.integration.tasks.database.ImportDbUnitDataTask
import com.xebialabs.gradle.integration.tasks.mq.StartMq
import com.xebialabs.gradle.integration.util.ExtensionsUtil
import com.xebialabs.gradle.integration.util.ProcessUtil
import com.xebialabs.gradle.integration.util.WorkerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class StartWorker extends DefaultTask {
    static NAME = "startWorker"
    String configurationName = 'integrationTestServer'

    StartWorker() {
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

    private def getEnv() {
        def extension = ExtensionsUtil.getExtension(project)
        def opts = "-Xmx1024m -DLOGFILE=deployit-worker"
        def suspend = extension.workerDebugSuspend ? 'y' : 'n'
        if (extension.workerDebugPort) {
            opts = "${opts} -agentlib:jdwp=transport=dt_socket,server=y,suspend=${suspend},address=${extension.workerDebugPort} "
        }

        if(extension.serverRuntimeDirectory != null) {
            def classpath = project.configurations.getByName(configurationName).filter { !it.name.endsWith("-sources.jar") }.asPath
            logger.debug("XL Deploy worker classpath: \n${classpath}")
            ["DEPLOYIT_SERVER_OPTS": opts.toString(), "DEPLOYIT_SERVER_CLASSPATH": classpath]
        }
        else {
            ["DEPLOYIT_SERVER_OPTS": opts.toString()]
        }
    }

    private def getBinDir() {
        Paths.get(ExtensionsUtil.getServerWorkingDir(project), "bin").toFile()
    }

    private void startWorker() {
        project.logger.lifecycle("Launching worker")
        def extension = ExtensionsUtil.getExtension(project)
        ProcessUtil.exec([
                command    : "run",
                params     : [
                        "worker",
                        "-master",
                        "127.0.0.1:${extension.akkaRemotingPort}".toString(),
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

    def waitForBoot() {
        project.logger.lifecycle("Waiting for worker to start")
        def extension = ExtensionsUtil.getExtension(project)
        int triesLeft = extension.serverPingTotalTries
        boolean success = false

        def runtimeDir = ExtensionsUtil.getServerWorkingDir(project)
        def workerLog = project.file("$runtimeDir/log/deployit-worker.log")

        while (triesLeft > 0 && !success) {
            try {
                workerLog.readLines().each { String line ->
                    if (line.contains("Registered successfully with Actor[akka://task-sys@127.0.0.1")) {
                        println("XL Deploy Worker successfully started.")
                        success = true
                    }
                }
            } catch (ignored) {
            }
            if (!success) {
                println("Waiting  ${extension.serverPingRetrySleepTime} second(s) for Worker startup. ($triesLeft)")
                TimeUnit.SECONDS.sleep(extension.serverPingRetrySleepTime)
                triesLeft -= 1
            }
        }
        if (!success) {
            throw new GradleException("Worker failed to start")
        }
    }

    @TaskAction
    void launch() {
        startWorker()
        waitForBoot()
    }
}
