package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.tasks.database.DatabaseStartTask
import com.xebialabs.gradle.integration.tasks.database.PrepareDatabaseTask
import com.xebialabs.gradle.integration.tasks.mq.StartMq
import com.xebialabs.gradle.integration.tasks.worker.StartWorkers
import com.xebialabs.gradle.integration.util.*
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP
import static com.xebialabs.gradle.integration.util.ShutdownUtil.shutdownServer

class StartIntegrationServerTask extends DefaultTask {
    static NAME = "startIntegrationServer"

    StartIntegrationServerTask() {
        def dependencies = [
                DownloadAndExtractServerDistTask.NAME,
                CopyOverlaysTask.NAME,
                SetLogbackLevelsTask.NAME,
                StartMq.NAME,
                RemoveStdoutConfigTask.NAME,
                PrepareDatabaseTask.NAME,
                DbUtil.isDerby(project) ? "derbyStart" : DatabaseStartTask.NAME,
                YamlPatchTask.NAME,
        ]

        this.configure {
            group = PLUGIN_GROUP
            dependsOn(dependencies)

            if (WorkerUtil.hasWorkers(project)) {
                finalizedBy(StartWorkers.NAME)
            }
        }
    }

    private def getBinDir() {
        Paths.get(ExtensionsUtil.getServerWorkingDir(project), "bin").toFile()
    }

    private void createConfFile() {
        project.logger.lifecycle("Creating deployit.conf file")

        def extension = ExtensionsUtil.getExtension(project)
        def file = project.file("${ExtensionsUtil.getServerWorkingDir(project)}/conf/deployit.conf")
        file.createNewFile()
        file.withWriter { w ->
            w.write("http.port=${extension.serverHttpPort}\n")
            w.write("http.bind.address=0.0.0.0\n")
            w.write("http.context.root=${extension.serverContextRoot}\n")
            w.write("threads.min=3\n")
            w.write("threads.max=24\n")
        }
    }

    private void initialize() {
        project.logger.lifecycle("Initializing Deploy")

        ProcessUtil.exec([
                command: "run",
                params : ["-setup", "-reinitialize", "-force", "-setup-defaults", "-force-upgrades", "conf/deployit.conf"],
                workDir: getBinDir(),
                wait   : true
        ])
    }

    private void startServer() {
        project.logger.lifecycle("Launching server")
        ProcessUtil.exec([
                command    : "run",
                params     : ["-force-upgrades"],
                environment: EnvironmentUtil.getEnv(project, "DEPLOYIT_SERVER_OPTS"),
                workDir    : getBinDir(),
                inheritIO  : true
        ])
    }

    private void waitForBoot() {
        project.logger.lifecycle("Waiting for server to start")
        def extension = ExtensionsUtil.getExtension(project)
        int triesLeft = extension.serverPingTotalTries
        boolean success = false
        while (triesLeft > 0 && !success) {
            try {
                def http = HTTPUtil.buildRequest("http://localhost:${extension.serverHttpPort}${extension.serverContextRoot}/deployit/metadata/type")
                http.get([:]) { resp, reader ->
                    println("XL Deploy successfully started on port ${extension.serverHttpPort}")
                    success = true
                }
            } catch (ignored) {
            }
            if (!success) {
                println("Waiting for ${extension.serverPingRetrySleepTime} second(s) before retry. ($triesLeft)")
                TimeUnit.SECONDS.sleep(extension.serverPingRetrySleepTime)
                triesLeft -= 1
            }
        }
        if (!success) {
            throw new GradleException("Server failed to start")
        }
    }

    private void startServerFromClasspath() {
        def classpath = project.configurations.getByName(ConfigurationsUtil.INTEGRATION_TEST_SERVER).filter { !it.name.endsWith("-sources.jar") }.asPath
        logger.debug("XL Deploy Server classpath: \n${classpath}")
        def extension = ExtensionsUtil.getExtension(project)

        project.logger.lifecycle("Starting integration test server on port ${extension.serverHttpPort} in runtime dir ${extension.serverRuntimeDirectory}")
        def jvmArgs = extension.serverJvmArgs
        def params = [fork: true, dir: extension.serverRuntimeDirectory, spawn: true, classname: "com.xebialabs.deployit.DeployitBootstrapper"]
        String jvmPath = project.properties['integrationServerJVMPath']
        if (jvmPath) {
            jvmPath = jvmPath + '/bin/java'
            params['jvm'] = jvmPath
            println("Using JVM from location: ${jvmPath}")
        }

        ant.java(params) {
            arg(value: '-force-upgrades')
            jvmArgs.each {
                jvmarg(value: it)
            }

            env(key: "CLASSPATH", value: classpath)

            if (extension.serverDebugPort != null) {
                println("Enabled debug mode on port ${extension.serverDebugPort}")
                jvmarg(value: "-Xdebug")
                jvmarg(value: "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=${extension.serverDebugPort}")
            }
        }
    }

    private void createFolders() {
        new File("${ExtensionsUtil.getServerWorkingDir(project)}/centralConfiguration").mkdirs()
    }

    @TaskAction
    void launch() {
        shutdownServer(project)
        createFolders()
        createConfFile()
        if (ExtensionsUtil.getExtension(project).serverRuntimeDirectory != null) {
            startServerFromClasspath()
        } else {
            initialize()
            startServer()
        }
        waitForBoot()
    }
}
