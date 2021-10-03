package ai.digital.integration.server.tasks.worker

import ai.digital.integration.server.domain.AkkaSecured
import ai.digital.integration.server.domain.Tls
import ai.digital.integration.server.domain.Worker
import ai.digital.integration.server.tasks.GenerateSecureAkkaKeysTask
import ai.digital.integration.server.tasks.TlsApplicationConfigurationOverrideTask
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

    void startWorker(Worker worker) {
        project.logger.lifecycle("Launching worker $worker.name")

        def hostName = CentralConfigurationUtil.readServerKey(project, "deploy.server.hostname")

        def params = [
                "-master",
                "127.0.0.1:${CentralConfigurationUtil.readServerKey(project, "deploy.server.port")}".toString(),
                "-api",
                "${ServerUtil.getUrl(project)}".toString(),
                "-hostname",
                hostName,
                "-name",
                worker.name,
                "-port",
                worker.port.toString()
        ]

        if (!worker.slimDistribution) {
            params = ["worker"] + params
        }

        if (ServerUtil.isAkkaSecured(project)) {
            def secured = SslUtil.getAkkaSecured(project, ServerUtil.getServerWorkingDir(project))
            def key = secured.keys[AkkaSecured.WORKER_KEY_NAME + worker.name]
            params += [
                "-keyStore",
                key.keyStoreFile().absolutePath,
                "-keyStorePassword",
                key.keyStorePassword,
                "-trustStore",
                secured.trustStoreFile().absolutePath,
                "-trustStorePassword",
                secured.truststorePassword,
            ]
            if (AkkaSecured.KEYSTORE_TYPE != "pkcs12") {
                params += [
                    "-keyPassword",
                    key.keyPassword,
                ]
            }
        }

        def environment = EnvironmentUtil.getEnv(
            project,
            "DEPLOYIT_SERVER_OPTS",
            worker.debugSuspend,
            worker.debugPort,
            logFileName(worker.name))
        project.logger.info("Starting worker with environment: $environment")
        Process process = ProcessUtil.exec([
                command    : "run",
                params     : params,
                environment: environment,
                workDir    : getBinDir(worker),
                discardIO  : worker.stdoutFileName ? false : true,
                redirectTo : worker.stdoutFileName ? "${getLogDir(worker)}/${worker.stdoutFileName}" : null,
        ])

        project.logger.lifecycle("Worker '${worker.name}' successfully started on PID [${process.pid()}] with command [${process.info().commandLine().orElse("")}].")

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
                spawn    : worker.stdoutFileName == null
        ]

        String jvmPath = project.properties['integrationServerJVMPath']
        if (jvmPath) {
            jvmPath = jvmPath + '/bin/java'
            params['jvm'] = jvmPath
            project.logger.lifecycle("Using JVM from location: ${jvmPath}")
        }

        def port = CentralConfigurationUtil.readServerKey(project, "deploy.server.port")
        def hostName = CentralConfigurationUtil.readServerKey(project, "deploy.server.hostname")

        def logDir = "${getLogDir(worker)}/${worker.stdoutFileName}"

        ant.java(params) {
            worker.jvmArgs.each {
                jvmarg(value: it)
            }
            jvmarg(value: "-DLOGFILE=${logFileName(worker.name)}")

            arg(value: "-master")
            arg(value: "${hostName}:${port}")
            arg(value: "-api")
            arg(value: ServerUtil.getUrl(project))
            arg(value: "-hostname")
            arg(value: "${hostName}")
            arg(value: "-port")
            arg(value: worker.port)
            arg(value: "-work")
            arg(value: worker.name)

            if (ServerUtil.isAkkaSecured(project)) {
                def secured = SslUtil.getAkkaSecured(project, ServerUtil.getServerWorkingDir(project))
                def key = secured.keys[AkkaSecured.WORKER_KEY_NAME + worker.name]

                arg(value: "-keyStore")
                arg(value: key.keyStoreFile().absolutePath)
                if (AkkaSecured.KEYSTORE_TYPE != "pkcs12") {
                    arg(value: "-keyPassword")
                    arg(value: key.keyPassword)
                }
                arg(value: "-keyStorePassword")
                arg(value: key.keyStorePassword)
                arg(value: "-trustStore")
                arg(value: secured.trustStoreFile().absolutePath)
                arg(value: "-trustStorePassword")
                arg(value: secured.truststorePasswor)
            }

            env(key: "CLASSPATH", value: classpath)

            if (worker.stdoutFileName) {
                redirector(
                        output: logDir
                )
            }

            if (worker.debugPort != null) {
                project.logger.lifecycle("Enabled debug mode on port ${worker.debugPort}")
                jvmarg(value: "-Xdebug")
                jvmarg(value: ServerUtil.createDebugString(worker.debugSuspend, worker.debugPort))
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
