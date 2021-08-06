package com.xebialabs.gradle.integration.tasks.worker

import com.xebialabs.gradle.integration.Worker
import com.xebialabs.gradle.integration.tasks.StartIntegrationServerTask
import com.xebialabs.gradle.integration.tasks.database.ImportDbUnitDataTask
import com.xebialabs.gradle.integration.tasks.mq.StartMq
import com.xebialabs.gradle.integration.util.*
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class StartWorkers extends DefaultTask {

    static NAME = "startWorkers"

    StartWorkers() {
        def dependencies = [
                StartMq.NAME
        ]

        this.configure {
            dependsOn(dependencies)
            group = PLUGIN_GROUP
            shouldRunAfter(StartIntegrationServerTask.NAME, ImportDbUnitDataTask.NAME, StartMq.NAME)
            onlyIf {
                WorkerUtil.hasWorkers(project)
            }
        }
    }

    private def getEnv(worker) {
        def opts = "-Xmx1024m -DLOGFILE=deployit-worker-${worker.name}"
        def suspend = worker.debugSuspend ? 'y' : 'n'
        if (worker.debugPort != null) {
            opts = "${opts} -agentlib:jdwp=transport=dt_socket,server=y,suspend=${suspend},address=${worker.debugPort} "
        }
        ["DEPLOYIT_SERVER_OPTS": opts.toString()]
    }


    private def getBinDir(worker) {
        Paths.get(WorkerUtil.getWorkerDir(worker, project), "bin").toFile()
    }

    void startWorker(worker) {
        project.logger.lifecycle("Launching worker")
        def extension = ExtensionsUtil.getExtension(project)

        ProcessUtil.exec([
                command    : "run",
                params     : [
                        "worker",
                        "-master",
                        "127.0.0.1:${CentralConfigurationUtil.readServerKey(project, "deploy.server.port")}".toString(),
                        "-api",
                        "http://localhost:${extension.serverHttpPort}".toString(),
                        "-name",
                        worker.name,
                        "-port",
                        worker.port.toString()
                ],
                environment: getEnv(worker),
                workDir    : getBinDir(worker)
        ])
        waitForBoot(WorkerUtil.getWorkerDir(worker, project), worker.name)
    }

    def waitForBoot(runtimeDir, workerName) {
        project.logger.lifecycle("Waiting for worker to start")
        def extension = ExtensionsUtil.getExtension(project)
        int triesLeft = extension.serverPingTotalTries
        boolean success = false

        def workerLog = project.file("$runtimeDir/log/deployit-worker-${workerName}.log")

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

    void startWorkerFromClasspath(Worker worker) {
        def classpath = project.configurations.getByName(ConfigurationsUtil.INTEGRATION_TEST_SERVER).filter { !it.name.endsWith("-sources.jar") }.asPath
        logger.debug("XL Deploy Worker classpath: \n${classpath}")
        def extension = ExtensionsUtil.getExtension(project)
        project.logger.lifecycle("Starting Worker test server for project ${project.name}. Remoting port: ${worker.port}")
        def jvmArgs = worker.jvmArgs
        def params = [fork: true, dir: WorkerUtil.getWorkerDir(worker, project), spawn: true, classname: "com.xebialabs.deployit.TaskExecutionEngineBootstrapper"]
        String jvmPath = project.properties['integrationServerJVMPath']
        if (jvmPath) {
            jvmPath = jvmPath + '/bin/java'
            params['jvm'] = jvmPath
            println("Using JVM from location: ${jvmPath}")
        }

        def port = CentralConfigurationUtil.readServerKey(project, "deploy.server.port")
        def hostName = CentralConfigurationUtil.readServerKey(project, "deploy.server.hostname")

        ant.java(params) {
            jvmArgs.each {
                jvmarg(value: it)
            }
            jvmarg(value: "-DLOGFILE=deployit-worker-${worker.name}")
            arg(value: "-master")
            arg(value: "${hostName}:${port}")
            arg(value: "-api")
            arg(value: "http://${hostName}:${extension.serverHttpPort}")
            arg(value: "-hostname")
            arg(value: "${hostName}")
            arg(value: "-port")
            arg(value: worker.port)
            arg(value: "-work")
            arg(value: worker.name)

            env(key: "CLASSPATH", value: classpath)

            if (worker.debugPort != null) {
                println("Enabled debug mode on port ${worker.debugPort}")
                jvmarg(value: "-Xdebug")
                jvmarg(value: "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=${worker.debugPort}")
            }
        }
        waitForBoot(WorkerUtil.getWorkerDir(worker, project), worker.name)
    }


    @TaskAction
    void launch() {
        def workers = project.extensions.getByName('workers')
        List<Worker> workerList = workers.collect().toList()
        workerList.each { worker ->
            if (worker.directory != null && !worker.directory.isEmpty())
                WorkerUtil.copyServerDirToWorkerDir(worker, project)

            if (ExtensionsUtil.getExtension(project).serverRuntimeDirectory != null) {
                startWorkerFromClasspath(worker)
            } else {
                startWorker(worker)
            }
        }
    }
}
