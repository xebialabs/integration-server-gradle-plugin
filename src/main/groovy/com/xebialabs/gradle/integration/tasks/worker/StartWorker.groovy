package com.xebialabs.gradle.integration.tasks.worker

import com.xebialabs.gradle.integration.tasks.StartIntegrationServerTask
import com.xebialabs.gradle.integration.tasks.database.ImportDbUnitDataTask
import com.xebialabs.gradle.integration.tasks.mq.StartMq
import com.xebialabs.gradle.integration.util.*
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
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
        def localWorker = project.hasProperty("localWorker") ? project.property("localWorker") : true
        project.logger.lifecycle("localWorker ${localWorker}")
        if (localWorker) {
            Paths.get(ExtensionsUtil.getServerWorkingDir(project), "bin").toFile()
        } else {
            def serverVersion = ExtensionsUtil.getExtension(project).serverVersion
            def targetDir = project.buildDir.toPath().resolve(PluginUtil.DIST_DESTINATION_NAME).toAbsolutePath().toString()
            def source = Paths.get(targetDir, "xl-deploy-${serverVersion}-server").toAbsolutePath().toString()
            def target = Paths.get(targetDir, "xl-deploy-${serverVersion}-worker").toAbsolutePath().toString()
            ProcessUtil.cpServerDirToWorkDir(project, source, target)
            Paths.get(target,"bin").toFile()
        }
    }


    private void startWorker() {
        project.logger.lifecycle("Launching worker")
        def extension = ExtensionsUtil.getExtension(project)
        def workBinDir = getBinDir()
        ProcessUtil.exec([
                command    : "run",
                params     : [
                        "worker",
                        "-master",
                        "127.0.0.1:${CentralConfigurationUtil.readServerKey(project, "deploy.server.port")}".toString(),
                        "-api",
                        "http://localhost:${extension.serverHttpPort}".toString(),
                        "-name",
                        "${extension.workerName}".toString(),
                        "-port",
                        "${extension.workerRemotingPort}".toString()
                ],
                environment: getEnv(),
                workDir    : workBinDir
        ])
        waitForBoot()
    }

    def waitForBoot() {
        project.logger.lifecycle("Waiting for worker to start")
        def extension = ExtensionsUtil.getExtension(project)
        int triesLeft = extension.serverPingTotalTries
        boolean success = false

        def runtimeDir = ExtensionsUtil.getWorkerWorkingDir(project)
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

        def port = CentralConfigurationUtil.readServerKey(project, "deploy.server.port")
        def hostName = CentralConfigurationUtil.readServerKey(project, "deploy.server.hostname")

        ant.java(params) {
            jvmArgs.each {
                jvmarg(value: it)
            }
            jvmarg(value: "-DLOGFILE=deployit-worker")
            arg(value: "-master")
            arg(value: "${hostName}:${port}")
            arg(value: "-api")
            arg(value: "http://${hostName}:${extension.serverHttpPort}")
            arg(value: "-hostname")
            arg(value: "${hostName}")
            arg(value: "-port")
            arg(value: ExtensionsUtil.findFreePort())
            arg(value: "-work")
            arg(value: extension.workerName)

            env(key: "CLASSPATH", value: classpath)

            if (extension.workerDebugPort != null) {
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
    }
}
