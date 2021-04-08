package com.xebialabs.gradle.integration.tasks

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import com.xebialabs.gradle.integration.tasks.database.DockerComposeDatabaseStartTask
import com.xebialabs.gradle.integration.tasks.database.PrepareDatabaseTask
import com.xebialabs.gradle.integration.util.DbUtil
import com.xebialabs.gradle.integration.util.ExtensionsUtil
import com.xebialabs.gradle.integration.util.HTTPUtil
import com.xebialabs.gradle.integration.util.ProcessUtil
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
                RemoveStdoutConfigTask.NAME,
                PrepareDatabaseTask.NAME,
                DbUtil.isDerby(project) ? "derbyStart" : DockerComposeDatabaseStartTask.NAME
        ]

        this.configure {
            group = PLUGIN_GROUP
            dependsOn(dependencies)
        }
    }

    private def getEnv() {
        def extension = ExtensionsUtil.getExtension(project)
        def opts = "-Xmx1024m -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
        def suspend = extension.serverDebugSuspend ? 'y' : 'n'
        if (extension.serverDebugPort) {
            opts = "${opts} -agentlib:jdwp=transport=dt_socket,server=y,suspend=${suspend},address=${extension.serverDebugPort}"
        }
        ["DEPLOYIT_SERVER_OPTS": opts.toString()]
    }

    private def getBinDir() {
        Paths.get(ExtensionsUtil.getServerWorkingDir(project), "bin").toFile()
    }

    private void writeConfFile() {
        project.logger.lifecycle("Writing deployit.conf file")

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

    private void writeXlDeployConf() {
        project.logger.lifecycle("Writing xl-deploy.conf file")
        def extension = ExtensionsUtil.getExtension(project)
        def defaultConf = project.file("${ExtensionsUtil.getServerWorkingDir(project)}/conf/xl-deploy.conf")
        def dbConfig = DbUtil.dbConfig(project).getObject("xl.repository.database").render()

        def cfgStr = """xl {
              server.hostname=localhost
              server.port = ${extension.akkaRemotingPort}

              repository.database $dbConfig
              
              reporting.database $dbConfig
            }
        """

        def config = ConfigFactory.parseString(cfgStr)
        def newConfig = config.withFallback(ConfigFactory.parseFile(defaultConf))
        defaultConf.text = newConfig.resolve().root().render(ConfigRenderOptions.concise())
    }

    private void initialize() {
        project.logger.lifecycle("Initializing XLD")

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
            environment: getEnv(),
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

    @TaskAction
    void launch() {
        shutdownServer(project)
        writeConfFile()
        writeXlDeployConf()
        initialize()
        startServer()
        waitForBoot()
    }
}
