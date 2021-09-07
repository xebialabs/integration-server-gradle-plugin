package ai.digital.integration.server.tasks.worker

import ai.digital.integration.server.domain.Worker
import ai.digital.integration.server.tasks.YamlPatchTask
import ai.digital.integration.server.tasks.mq.StartMqTask
import ai.digital.integration.server.util.CentralConfigurationUtil
import ai.digital.integration.server.util.ConfigurationsUtil
import ai.digital.integration.server.util.EnvironmentUtil
import ai.digital.integration.server.util.ExtensionUtil
import ai.digital.integration.server.util.ProcessUtil
import ai.digital.integration.server.util.ServerUtil
import ai.digital.integration.server.util.WaitForBootUtil
import ai.digital.integration.server.util.WorkerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class StartWorkersTask extends DefaultTask {

    static NAME = "startWorkers"

    StartWorkersTask() {

        def slimDependencies = WorkerUtil.hasSlimWorkers(project) ? [
                CopyIntegrationServerTask.NAME
        ] : []

        def nonSlimDependencies = WorkerUtil.hasNonSlimWorkers(project) ? [
                DownloadAndExtractWorkerDistTask.NAME,
                SyncServerPluginsWithWorkerTask.NAME
        ] : []

        def dependencies = slimDependencies + nonSlimDependencies + [
                SetWorkersLogbackLevelsTask.NAME,
                WorkerOverlaysTask.NAME,
                StartMqTask.NAME,
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
        Paths.get(WorkerUtil.getWorkerWorkingDir(project, worker), "bin").toFile()
    }

    private def getLogDir(Worker worker) {
        Paths.get(WorkerUtil.getWorkerWorkingDir(project, worker), "log").toFile()
    }

    private static def logFileName(String workerName) {
        "deploy-worker-${workerName}"
    }

    void startWorker(Worker worker) {
        project.logger.lifecycle("Launching worker $worker.name")
        def server = ServerUtil.getServer(project)

        def params = [
            "-master",
            "127.0.0.1:${CentralConfigurationUtil.readServerKey(project, "deploy.server.port")}".toString(),
            "-api",
            "http://localhost:${server.httpPort}".toString(),
            "-name",
            worker.name,
            "-port",
            worker.port.toString()
        ]

        if (worker.slimDistribution) {
            params = [ "worker" ] + params
        }

        Process process = ProcessUtil.exec([
                command    : "run",
                params     : params,
                environment: EnvironmentUtil.getEnv("DEPLOYIT_SERVER_OPTS",
                        worker.debugSuspend,
                        worker.debugPort,
                        logFileName(worker.name)),
                workDir    : getBinDir(worker),
                discardIO  : worker.outputFilename ? false : true,
                redirectTo : worker.outputFilename ? "${getLogDir(worker)}/${worker.outputFilename}" : null,
        ])

        project.logger.lifecycle("Worker '${worker.name}' successfully started: [${process.pid()}] [${process.info().commandLine().orElse("")}].")

        waitForBoot(worker, process)
    }

    void startWorkerFromClasspath(Worker worker) {
        def classpath = project.configurations
                .getByName(ConfigurationsUtil.DEPLOY_SERVER)
                .filter { !it.name.endsWith("-sources.jar") }.asPath

        logger.debug("XL Deploy Worker classpath: \n${classpath}")
        project.logger.lifecycle("Starting Worker ${worker.name} for project ${project.name} on a port: ${worker.port}")

        def params = [
                classname: "com.xebialabs.deployit.TaskExecutionEngineBootstrapper",
                dir      : WorkerUtil.getWorkerWorkingDir(project, worker),
                fork     : true,
                spawn    : worker.outputFilename == null
        ]

        String jvmPath = project.properties['integrationServerJVMPath']
        if (jvmPath) {
            jvmPath = jvmPath + '/bin/java'
            params['jvm'] = jvmPath
            project.logger.lifecycle("Using JVM from location: ${jvmPath}")
        }

        def port = CentralConfigurationUtil.readServerKey(project, "deploy.server.port")
        def hostName = CentralConfigurationUtil.readServerKey(project, "deploy.server.hostname")

        def logDir = "${getLogDir(worker)}/${worker.outputFilename}"

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

            if (worker.outputFilename) {
                redirector(
                    output: logDir
                )
            }

            if (worker.debugPort != null) {
                project.logger.lifecycle("Enabled debug mode on port ${worker.debugPort}")
                jvmarg(value: "-Xdebug")
                jvmarg(value: "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=${worker.debugPort}")
            }
        }
        waitForBoot(worker, null)
    }

    @TaskAction
    void launch() {
        def workers = ExtensionUtil.getExtension(project).workers
        workers.each { Worker worker ->
            if (WorkerUtil.hasRuntimeDirectory(project, worker)) {
                startWorkerFromClasspath(worker)
            } else {
                startWorker(worker)
            }
        }
    }

    private void waitForBoot(Worker worker, Process process) {
        def workerLog = project.file("${WorkerUtil.getWorkerWorkingDir(project, worker)}/log/${logFileName(worker.name)}.log")
        def containsLine = "Registered successfully with Actor[akka://task-sys@127.0.0.1"
        WaitForBootUtil.byLog(project, "worker ${worker.name}", workerLog, containsLine, process)
    }
}
