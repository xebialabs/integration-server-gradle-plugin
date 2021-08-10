package ai.digital.integration.server.tasks

import ai.digital.integration.server.util.ConfigurationsUtil
import ai.digital.integration.server.util.DbUtil
import ai.digital.integration.server.util.EnvironmentUtil
import ai.digital.integration.server.util.LocationUtil
import ai.digital.integration.server.util.ProcessUtil
import ai.digital.integration.server.util.SatelliteUtil
import ai.digital.integration.server.util.ServerUtil
import ai.digital.integration.server.util.WaitForBootUtil
import ai.digital.integration.server.util.WorkerUtil
import ai.digital.integration.server.domain.Server
import ai.digital.integration.server.tasks.database.DatabaseStartTask
import ai.digital.integration.server.tasks.database.PrepareDatabaseTask
import ai.digital.integration.server.tasks.mq.StartMqTask
import ai.digital.integration.server.tasks.satellite.StartSatelliteTask
import ai.digital.integration.server.tasks.worker.StartWorkersTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import static ai.digital.integration.server.util.ShutdownUtil.shutdownServer

class StartIntegrationServerTask extends DefaultTask {
    static NAME = "startIntegrationServer"

    StartIntegrationServerTask() {
        def dependencies = [
                DownloadAndExtractServerDistTask.NAME,
                CopyOverlaysTask.NAME,
                SetLogbackLevelsTask.NAME,
                StartMqTask.NAME,
                RemoveStdoutConfigTask.NAME,
                PrepareDatabaseTask.NAME,
                DbUtil.isDerby(project) ? "derbyStart" : DatabaseStartTask.NAME,
                YamlPatchTask.NAME
        ]

        this.configure {
            group = PLUGIN_GROUP
            dependsOn(dependencies)

            if (WorkerUtil.hasWorkers(project)) {
                finalizedBy(StartWorkersTask.NAME)
            }

            if (SatelliteUtil.hasSatellites(project)) {
                finalizedBy(StartSatelliteTask.NAME)
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
            project.logger.lifecycle("Using JVM from location: ${jvmPath}")
        }


        ant.java(params) {
            arg(value: '-force-upgrades')
            server.jvmArgs.each {
                jvmarg(value: it)
            }

            env(key: "CLASSPATH", value: classpath)

            if (server.debugPort != null) {
                project.logger.lifecycle("Enabled debug mode on port ${server.debugPort}")
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
        if (!hasToBeStartedFromClasspath(server)) {
            initialize()
        }
    }

    private def start(Server server) {
        if (hasToBeStartedFromClasspath(server)) {
            startServerFromClasspath(server)
        } else {
            startServer(server)
        }
    }

    private def waitForBoot(Server server) {
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
