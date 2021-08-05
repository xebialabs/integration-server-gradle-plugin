package com.xebialabs.gradle.integration.tasks.worker

import com.xebialabs.gradle.integration.tasks.StartIntegrationServerTask
import com.xebialabs.gradle.integration.tasks.database.ImportDbUnitDataTask
import com.xebialabs.gradle.integration.tasks.mq.StartMq
import com.xebialabs.gradle.integration.util.*
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class StartWorker extends DefaultTask {
    @Input
    Integer debugPort

    @Input
    Boolean debugSuspend

    @Input
    String[] jvmArgs = []

    @Input
    String name

    @Input
    Integer port

    @Input
    Boolean directoryLocal

    @Input
    String directory

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
        def opts = "-Xmx1024m -DLOGFILE=deployit-worker"
        def suspend = getDebugSuspend() ? 'y' : 'n'
        if (getDebugPort() != 0) {
            opts = "${opts} -agentlib:jdwp=transport=dt_socket,server=y,suspend=${suspend},address=${getDebugPort()} "
        }
        ["DEPLOYIT_SERVER_OPTS": opts.toString()]

    }


    private def getBinDir() {
        Paths.get(WorkerUtil.getWorkerDir(project), "bin").toFile()
    }

    private void startWorker() {
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
                        "${getName()}".toString(),
                        "-port",
                        "${getPort()}".toString()
                ],
                environment: getEnv(),
                workDir    : getBinDir()
        ])
        waitForBoot(getWorkerDir(project))
    }

    def waitForBoot(runtimeDir) {
        project.logger.lifecycle("Waiting for worker to start")
        def extension = ExtensionsUtil.getExtension(project)
        int triesLeft = extension.serverPingTotalTries
        boolean success = false

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
        project.logger.lifecycle("Starting Worker test server for project ${project.name}. Remoting port: ${getPort()}")
        def jvmArgs = getJvmArgs()
        def params = [fork: true, dir: getWorkerDir(project), spawn: true, classname: "com.xebialabs.deployit.TaskExecutionEngineBootstrapper"]
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
            arg(value: getPort())
            arg(value: "-work")
            arg(value: getName())

            env(key: "CLASSPATH", value: classpath)

            if (getDebugPort() != null) {
                println("Enabled debug mode on port ${getDebugPort()}")
                jvmarg(value: "-Xdebug")
                jvmarg(value: "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=${getDebugPort()}")
            }
        }
        waitForBoot(getWorkerDir(project))
    }

    private def getWorkerDir(project) {
        if (!getDirectoryLocal()) {
            getDirectory()
        } else {
            ExtensionsUtil.getServerWorkingDir(project)
        }
    }


    void copyServerDirToWorkerDir() {
        def sourceDir = Paths.get(ExtensionsUtil.getServerWorkingDir(project)).toFile()
        def destinationDir = Paths.get(ExtensionsUtil.getExtension(project).workerRuntimeDirectory).toFile()
        destinationDir.setExecutable(true)
        FileUtils.copyDirectory(sourceDir, destinationDir);
        ProcessUtil.chMod(project, "755", "${destinationDir.getAbsolutePath().toString()}")
    }

    @TaskAction
    void launch() {
        if(!directoryLocal){
            copyServerDirToWorkerDir()
        }
        if (ExtensionsUtil.getExtension(project).serverRuntimeDirectory != null) {
            startWorkerFromClasspath()
        } else {
            startWorker()
        }
    }
}
