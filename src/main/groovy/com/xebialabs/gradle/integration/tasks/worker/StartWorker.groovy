package com.xebialabs.gradle.integration.tasks.worker

import com.xebialabs.gradle.integration.tasks.StartIntegrationServerTask
import com.xebialabs.gradle.integration.tasks.database.ImportDbUnitDataTask
import com.xebialabs.gradle.integration.tasks.mq.StartMq
import com.xebialabs.gradle.integration.util.ConfigurationsUtil
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
        ["DEPLOYIT_SERVER_OPTS": opts.toString()]

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

    private void startWorkerFromClasspath() {
        def classpath = project.configurations.getByName(ConfigurationsUtil.INTEGRATION_TEST_SERVER).filter { !it.name.endsWith("-sources.jar") }.asPath
        logger.debug("XL Deploy Worker classpath: \n${classpath}")
        def extension = ExtensionsUtil.getExtension(project)
        project.logger.lifecycle("Starting Worker test server for project ${project.name}. Remoting port: ${extension.workerRemotingPort}")
        def jvmArgs = extension.workerJvmArgs
        def params = [fork: true, dir: extension.serverRuntimeDirectory, spawn: true, classname: "com.xebialabs.deployit.TaskExecutionEngineBootstrapper"]
        String jvmPath = project.properties['integrationServerJVMPath']
        if (jvmPath) {
            jvmPath = jvmPath + '/bin/java'
            params['jvm'] = jvmPath
            println("Using JVM from location: ${jvmPath}")
        }

      ant.java(params) {
            jvmArgs.each {
                jvmarg(value: it)
            }
            jvmarg(value: "-DLOGFILE=deployit-worker")
            arg(value: "-master")
            arg(value: "127.0.0.1:${extension.akkaRemotingPort.toString()}")
            arg(value: "-api")
            arg(value: "http://localhost:${extension.serverHttpPort.toString()}")
            arg(value: "-hostname")
            arg(value: "localhost")
            arg(value: "-port")
            arg(value: extension.workerRemotingPort.toString())
            arg(value: "-work")
            arg(value: extension.workerName)

            env(key: "CLASSPATH", value: classpath)

            if (extension.workerDebugPort!=null) {
                println("Enabled debug mode on port ${extension.workerDebugPort}")
                jvmarg(value: "-Xdebug")
                jvmarg(value: "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=${extension.workerDebugPort}")
            }
        }
    }


    @TaskAction
    void launch() {
        if (ExtensionsUtil.getExtension(project).serverRuntimeDirectory != null) {
            startWorkerFromClasspath()
        } else {
            startWorker()
        }

        waitForBoot()
    }
}
