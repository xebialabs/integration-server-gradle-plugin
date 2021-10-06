package ai.digital.integration.server.tasks.worker

import ai.digital.integration.server.domain.Worker
import ai.digital.integration.server.tasks.YamlPatchTask
import ai.digital.integration.server.tasks.mq.StartMqTask
import ai.digital.integration.server.util.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class StartWorkersTask extends DefaultTask {

    public static String NAME = "startWorkers"

    StartWorkersTask() {

        def nonSlimDependencies = WorkerUtil.hasNonSlimWorkers(project) ? [
                CopyIntegrationServerTask.NAME
        ] : []

        def slimDependencies = WorkerUtil.hasSlimWorkers(project) ? [
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

    Process startWorker(Worker worker) {
        project.logger.lifecycle("Launching worker $worker.name")

        def hostName = CentralConfigurationUtil.readServerKey(project, "deploy.server.hostname")
        def port = CentralConfigurationUtil.readServerKey(project, "deploy.server.port")
        def params = WorkerUtil.composeProgramParams(project, worker, hostName as String, port as String, true)

        def environment = EnvironmentUtil.getEnv(
            project,
            "DEPLOYIT_SERVER_OPTS",
            worker.debugSuspend,
            worker.debugPort,
            logFileName(worker.name))

        project.logger.info("Starting worker with environment: $environment")

        ProcessUtil.exec([
                command    : "run",
                params     : params,
                environment: environment,
                workDir    : getBinDir(worker),
                discardIO  : worker.stdoutFileName ? false : true,
                redirectTo : worker.stdoutFileName ? new File("${getLogDir(worker)}/${worker.stdoutFileName}") : null,
        ])
    }

    Process startWorkerFromClasspath(Worker worker) {
        def classpath = project.configurations
                .getByName(ConfigurationsUtil.DEPLOY_SERVER)
                .filter { !it.name.endsWith("-sources.jar") }.asPath

        logger.debug("XL Deploy Worker classpath: \n${classpath}")

        def hostName = CentralConfigurationUtil.readServerKey(project, "deploy.server.hostname")
        def port = CentralConfigurationUtil.readServerKey(project, "deploy.server.port")
        def programArgs = WorkerUtil.composeProgramParams(project, worker, hostName as String, port as String, false)

        def jvmArgs = worker.jvmArgs.toList()
        jvmArgs += [ "-DLOGFILE=${logFileName(worker.name)}".toString() ]
        if (worker.debugPort) {
            jvmArgs.addAll(JavaUtil.debugJvmArg(project, worker.debugPort, worker.debugSuspend))
        }

        if (DeployServerUtil.isTls(project)) {
            def tls = SslUtil.getTls(project, DeployServerUtil.getServerWorkingDir(project))
            jvmArgs.addAll([
                "-Djavax.net.ssl.trustStore=${tls?.trustStoreFile()}".toString(),
                "-Djavax.net.ssl.trustStorePassword=${tls?.truststorePassword}".toString()
            ])
        }

        def config = [
            classpath : classpath,
            discardIO  : worker.stdoutFileName ? false : true,
            jvmArgs : jvmArgs,
            mainClass : "com.xebialabs.deployit.TaskExecutionEngineBootstrapper",
            programArgs    : programArgs,
            redirectTo : worker.stdoutFileName ? new File("${getLogDir(worker)}/${worker.stdoutFileName}") : null,
            workDir    : new File(WorkerUtil.getWorkerWorkingDir(project, worker)),
        ]

        if (project.properties["integrationServerJVMPath"]) {
            config.putAll(JavaUtil.jvmPath(project, project.properties["integrationServerJVMPath"] as String))
        }

        project.logger.lifecycle("Starting Worker ${worker.name} for project ${project.name} on a port: ${worker.port}")

        JavaUtil.execJava(config)
    }

    void waitForBoot(Worker worker, Process process) {
        def workerLog = project.file("${WorkerUtil.getWorkerWorkingDir(project, worker)}/log/${logFileName(worker.name)}.log")
        def containsLine = "Registered successfully with Actor[akka://task-sys@127.0.0.1"
        WaitForBootUtil.byLog(project, "worker ${worker.name}", workerLog, containsLine, process)
    }

    @TaskAction
    void launch() {
        def workers = ExtensionUtil.getExtension(project).workers
        workers.each { Worker worker ->

            Process process
            if (WorkerUtil.hasRuntimeDirectory(project, worker)) {
                process = startWorkerFromClasspath(worker)
            } else {
                process = startWorker(worker)
            }

            project.logger.lifecycle("Worker '${worker.name}' successfully started on PID [${process.pid()}] with command [${process.info().commandLine().orElse("")}].")

            waitForBoot(worker, process)
        }
    }
}
