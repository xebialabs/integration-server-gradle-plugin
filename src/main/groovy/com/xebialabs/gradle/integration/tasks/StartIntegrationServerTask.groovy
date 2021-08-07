package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.domain.Server
import com.xebialabs.gradle.integration.tasks.database.DatabaseStartTask
import com.xebialabs.gradle.integration.tasks.database.PrepareDatabaseTask
import com.xebialabs.gradle.integration.tasks.mq.StartMq
import com.xebialabs.gradle.integration.tasks.worker.StartWorkers
import com.xebialabs.gradle.integration.util.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

import static com.xebialabs.gradle.integration.constant.PluginConstant.PLUGIN_GROUP
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
        Paths.get(LocationUtil.getServerWorkingDir(project), "bin").toFile()
    }

    private void createConfFile(Server server) {
        project.logger.lifecycle("Creating deployit.conf file")

        def file = project.file("${LocationUtil.getServerWorkingDir(project)}/conf/deployit.conf")
        file.createNewFile()
        file.withWriter { BufferedWriter w ->
            w.write("http.port=${server.httpPort}\n")
            w.write("http.bind.address=0.0.0.0\n")
            w.write("http.context.root=${server.contextRoot}\n")
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

    private void startServer(Server server) {
        project.logger.lifecycle("Launching server")
        ProcessUtil.exec([
                command    : "run",
                params     : ["-force-upgrades"],
                environment: EnvironmentUtil.getServerEnv(server),
                workDir    : getBinDir(),
                inheritIO  : true
        ])
    }

    private void startServerFromClasspath(Server server) {
        project.logger.lifecycle("startServerFromClasspath.")
        def classpath = project.configurations.getByName(ConfigurationsUtil.DEPLOY_SERVER).filter { !it.name.endsWith("-sources.jar") }.asPath

        project.logger.lifecycle("Launching Deploy Server from classpath ${classpath}.")
        project.logger.lifecycle("Starting integration test server on port ${server.httpPort} from runtime dir ${server.runtimeDirectory}")

        def params = [fork: true, dir: server.runtimeDirectory, spawn: true, classname: "com.xebialabs.deployit.DeployitBootstrapper"]
        String jvmPath = project.properties['integrationServerJVMPath']
        if (jvmPath) {
            jvmPath = jvmPath + '/bin/java'
            params['jvm'] = jvmPath
            println("Using JVM from location: ${jvmPath}")
        }


        ant.java(params) {
            arg(value: '-force-upgrades')
            server.jvmArgs.each {
                jvmarg(value: it)
            }

            env(key: "CLASSPATH", value: classpath)

            if (server.debugPort != null) {
                println("Enabled debug mode on port ${server.debugPort}")
                jvmarg(value: "-Xdebug")
                jvmarg(value: "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=${server.debugPort}")
            }
        }
    }

    private void createFolders() {
        project.logger.lifecycle("Creating folders for central configuration files.")
        new File("${LocationUtil.getServerWorkingDir(project)}/centralConfiguration").mkdirs()
    }

    private static def hasToBeStartedFromClasspath(Server server) {
        server.runtimeDirectory != null
    }

    private def prepare(Server server) {
        project.logger.lifecycle("Preparing serve ${server.name} before launching it.")
        createFolders()
        createConfFile(server)
        project.logger.lifecycle("----- 1 --------")

        if (!hasToBeStartedFromClasspath(server)) {
            project.logger.lifecycle("----- 2 --------")
            initialize()
        }
    }

    private def start(Server server) {
        project.logger.lifecycle("----- 3 --------")
        if (hasToBeStartedFromClasspath(server)) {
            project.logger.lifecycle("----- 4 --------")
            startServerFromClasspath(server)
        } else {
            project.logger.lifecycle("----- 5 --------")
            startServer(server)
        }
    }

    private def waitForBoot(server) {
        def url = "http://localhost:${server.httpPort}${server.contextRoot}/deployit/metadata/type"
        WaitForBootUtil.byPort(project, "Deploy", url, server.httpPort)
    }

    private def maybeTearDown() {
        shutdownServer(project)
    }

    @TaskAction
    void launch() {
        def server = ServerUtil.getServer(project)
        project.logger.lifecycle("About to launch Deploy Server on port ${server.httpPort}.")

        maybeTearDown()
        prepare(server)
        start(server)
        waitForBoot(server)
    }
}
