package ai.digital.integration.server.tasks.worker

import ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.domain.Worker
import ai.digital.integration.server.tasks.YamlPatchTask
import ai.digital.integration.server.tasks.mq.StartMqTask
import ai.digital.integration.server.util.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Paths

abstract class StartWorkersTask : DefaultTask() {

    companion object {
        @JvmStatic
        val NAME = "startWorkers"
    }

    init {
        this.group = PLUGIN_GROUP

        if (WorkerUtil.hasNonSlimWorkers(project)) {
            this.dependsOn(CopyIntegrationServerTask.NAME)
        }

        if (WorkerUtil.hasSlimWorkers(project)) {
            this.dependsOn(DownloadAndExtractWorkerDistTask.NAME)
            this.dependsOn(SyncServerPluginsWithWorkerTask.NAME)
        }

        this.dependsOn(SetWorkersLogbackLevelsTask.NAME)
        this.dependsOn(StartMqTask.NAME)
        this.dependsOn(WorkerOverlaysTask.NAME)
        this.dependsOn(YamlPatchTask.NAME)

        this.onlyIf {
            WorkerUtil.hasWorkers(project)
        }
    }

    private fun getBinDir(worker: Worker): File {
        return Paths.get(WorkerUtil.getWorkerWorkingDir(project, worker), "bin").toFile()
    }

    private fun getLogDir(worker: Worker): File {
        return Paths.get(WorkerUtil.getWorkerWorkingDir(project, worker), "log").toFile()
    }

    private fun logFileName(workerName: String): String {
        return "deploy-worker-${workerName}"
    }

    private fun startWorker(worker: Worker): Process {
        project.logger.lifecycle("Launching worker $worker.name")

        val hostName = CentralConfigurationUtil.readServerKey(project, "deploy.server.hostname")
        val port = CentralConfigurationUtil.readServerKey(project, "deploy.server.port")
        val params = WorkerUtil.composeProgramParams(project, worker, hostName, port, true)

        val environment = EnvironmentUtil.getEnv(
            project,
            "DEPLOYIT_SERVER_OPTS",
            worker.debugSuspend,
            worker.debugPort,
            logFileName(worker.name))

        project.logger.info("Starting worker with environment: $environment")

        return ProcessUtil.exec(mapOf(
            "command" to "run",
            "params" to params,
            "environment" to environment,
            "workDir" to getBinDir(worker),
            "discardIO" to worker.stdoutFileName.isNullOrEmpty(),
            "redirectTo" to (
                    if (!worker.stdoutFileName.isNullOrEmpty()) File("${getLogDir(worker)}/${worker.stdoutFileName}") else null)
        ))
    }

    private fun startWorkerFromClasspath(worker: Worker): Process {
        val classpath = project.configurations.getByName(ConfigurationsUtil.DEPLOY_SERVER)
            .filter { !it.name.endsWith("-sources.jar") }.asPath

        logger.debug("XL Deploy Worker classpath: \n${classpath}")

        val hostName = CentralConfigurationUtil.readServerKey(project, "deploy.server.hostname")
        val port = CentralConfigurationUtil.readServerKey(project, "deploy.server.port")
        val programArgs = WorkerUtil.composeProgramParams(project, worker, hostName, port, false)

        val jvmArgs = worker.jvmArgs.toList().toMutableList()
        jvmArgs.add("-DLOGFILE=${logFileName(worker.name)}")

        worker.debugPort?.let { dPort ->
            jvmArgs.addAll(JavaUtil.debugJvmArg(project, dPort, worker.debugSuspend))
        }


        if (DeployServerUtil.isTls(project)) {
            val tls = SslUtil.getTls(project, DeployServerUtil.getServerWorkingDir(project))
            jvmArgs.addAll(arrayOf(
                "-Djavax.net.ssl.trustStore=${tls?.trustStoreFile()}",
                "-Djavax.net.ssl.trustStorePassword=${tls?.truststorePassword}"
            ))
        }

        val config = mutableMapOf(
            "classpath" to classpath,
            "discardIO" to !worker.stdoutFileName.isNullOrBlank(),
            "jvmArgs" to jvmArgs,
            "mainClass" to "com.xebialabs.deployit.TaskExecutionEngineBootstrapper",
            "programArgs" to programArgs,
            "redirectTo" to (
                    if (!worker.stdoutFileName.isNullOrEmpty()) File("${getLogDir(worker)}/${worker.stdoutFileName}") else null),
            "workDir" to File(WorkerUtil.getWorkerWorkingDir(project, worker))
        )

        if (project.hasProperty("integrationServerJVMPath")) {
            config.putAll(JavaUtil.jvmPath(project, project.property("integrationServerJVMPath").toString()))
        }

        project.logger.lifecycle("Starting Worker ${worker.name} for project ${project.name} on a port: ${worker.port}")

        return JavaUtil.execJava(config)
    }

    private fun waitForBoot(worker: Worker, process: Process) {
        val workerLog =
            project.file("${WorkerUtil.getWorkerWorkingDir(project, worker)}/log/${logFileName(worker.name)}.log")
        val containsLine = "Registered successfully with Actor[akka://task-sys@127.0.0.1"
        WaitForBootUtil.byLog(project, "worker ${worker.name}", workerLog, containsLine, process)
    }

    @TaskAction
    fun launch() {
        val workers = ExtensionUtil.getExtension(project).workers
        workers.forEach { worker ->

            val process  = if (WorkerUtil.hasRuntimeDirectory(project, worker)) {
                startWorkerFromClasspath(worker)
            } else {
                startWorker(worker)
            }

            project.logger.lifecycle("Worker '${worker.name}' successfully started on PID [${process.pid()}] with command [${
                process.info().commandLine().orElse("")
            }].")

            waitForBoot(worker, process)
        }
    }
}
