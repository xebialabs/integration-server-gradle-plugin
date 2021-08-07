package com.xebialabs.gradle.integration.tasks.worker

import com.xebialabs.gradle.integration.domain.Worker
import com.xebialabs.gradle.integration.tasks.YamlPatchTask
import com.xebialabs.gradle.integration.tasks.mq.StartMq
import com.xebialabs.gradle.integration.util.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

import static com.xebialabs.gradle.integration.constant.PluginConstant.PLUGIN_GROUP

class StartWorkers extends DefaultTask {

    static NAME = "startWorkers"

    StartWorkers() {
        def dependencies = [
                StartMq.NAME,
                YamlPatchTask.NAME
        ]

        this.configure {
            dependsOn(dependencies)
            group = PLUGIN_GROUP
            onlyIf {
                WorkerUtil.hasWorkers(project)
            }
        }
    }

    private def getBinDir(Worker worker) {
        Paths.get(WorkerUtil.getWorkerDir(worker, project), "bin").toFile()
    }

    private static def logFileName(String workerName) {
        "deploy-worker-${workerName}"
    }

    void startWorker(Worker worker) {
        project.logger.lifecycle("Launching worker $worker.name")
        def server = ServerUtil.getServer(project)

        ProcessUtil.exec([
                command    : "run",
                params     : [
                        "worker",
                        "-master",
                        "127.0.0.1:${CentralConfigurationUtil.readServerKey(project, "deploy.server.port")}".toString(),
                        "-api",
                        "http://localhost:${server.httpPort}".toString(),
                        "-name",
                        worker.name,
                        "-port",
                        worker.port.toString()
                ],
                environment: EnvironmentUtil.getEnv("DEPLOYIT_SERVER_OPTS",
                        worker.debugSuspend,
                        worker.debugPort,
                        logFileName(worker.name)),
                workDir    : getBinDir(worker)
        ])

        waitForBoot(worker)
    }

    void startWorkerFromClasspath(Worker worker) {
        def classpath = project.configurations
                .getByName(ConfigurationsUtil.DEPLOY_SERVER)
                .filter { !it.name.endsWith("-sources.jar") }.asPath

        logger.debug("XL Deploy Worker classpath: \n${classpath}")
        project.logger.lifecycle("Starting Worker for project ${project.name} on a port: ${worker.port}")

        def params = [
                classname: "com.xebialabs.deployit.TaskExecutionEngineBootstrapper",
                dir      : WorkerUtil.getWorkerDir(worker, project),
                fork     : true,
                spawn    : true
        ]

        String jvmPath = project.properties['integrationServerJVMPath']
        if (jvmPath) {
            jvmPath = jvmPath + '/bin/java'
            params['jvm'] = jvmPath
            project.logger.lifecycle("Using JVM from location: ${jvmPath}")
        }

        def port = CentralConfigurationUtil.readServerKey(project, "deploy.server.port")
        def hostName = CentralConfigurationUtil.readServerKey(project, "deploy.server.hostname")

        ant.java(params) {
            worker.jvmArgs.each {
                jvmarg(value: it)
            }
            jvmarg(value: "-DLOGFILE=${logFileName(worker.name)}")
            arg(value: "-master")
            arg(value: "${hostName}:${port}")
            arg(value: "-api")
            arg(value: "http://${hostName}:${ServerUtil.getServer(project).httpPort}")
            arg(value: "-hostname")
            arg(value: "${hostName}")
            arg(value: "-port")
            arg(value: worker.port)
            arg(value: "-work")
            arg(value: worker.name)

            env(key: "CLASSPATH", value: classpath)

            if (worker.debugPort != null) {
                project.logger.lifecycle("Enabled debug mode on port ${worker.debugPort}")
                jvmarg(value: "-Xdebug")
                jvmarg(value: "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=${worker.debugPort}")
            }
        }
        waitForBoot(worker)
    }

    @TaskAction
    void launch() {
        def workers = ExtensionUtil.getExtension(project).workers
        workers.each { Worker worker ->
            if (WorkerUtil.isExternalWorker(worker))
                WorkerUtil.copyServerDirToWorkerDir(worker, project)
            if (WorkerUtil.hasRuntimeDirectory(project)) {
                startWorkerFromClasspath(worker)
            } else {
                startWorker(worker)
            }
        }
    }

    private void waitForBoot(Worker worker) {
        def workerLog = project.file("${WorkerUtil.getWorkerDir(worker, project)}/log/${logFileName(worker.name)}.log")
        def containsLine = "Registered successfully with Actor[akka://task-sys@127.0.0.1"
        WaitForBootUtil.byLog(project, "worker ${worker.name}", workerLog, containsLine)
    }
}
