package com.xebialabs.gradle.integration.tasks.centralconfig


import com.xebialabs.gradle.integration.util.ExtensionsUtil
import com.xebialabs.gradle.integration.util.HTTPUtil
import com.xebialabs.gradle.integration.util.ProcessUtil
import com.xebialabs.gradle.integration.util.FileUtil
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.impldep.org.yaml.snakeyaml.DumperOptions
import org.gradle.internal.impldep.org.yaml.snakeyaml.Yaml

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.concurrent.TimeUnit

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class StartConfigServerTask extends DefaultTask {
    static NAME = "StartConfigServer"

    StartConfigServerTask() {

        def dependencies = [
                DownloadAndExtractConfigServerDistTask.NAME
        ]
        this.configure {
            group = PLUGIN_GROUP
            dependsOn(dependencies)
        }
    }

    private def getEnv() {
        def extension = ExtensionsUtil.getExtension(project)
        def opts = "-Xmx1024m"
        def suspend = extension.configServerDebugSuspend ? 'y' : 'n'
        if (extension.configServerDebugPort) {
            opts = "${opts} -agentlib:jdwp=transport=dt_socket,server=y,suspend=${suspend},address=${extension.configServerDebugPort}"
        }
        ["CONFIG_SERVER_OPTS": opts.toString()]
    }

    private def getBinDir() {
        Paths.get(ExtensionsUtil.getConfigServerWorkingDir(project), "bin").toFile()
    }

    private void startConfigServer() {
        project.logger.lifecycle("Launching Central Configuration Standalone")
        ProcessUtil.exec([
                command    : "run",
                environment: getEnv(),
                workDir    : getBinDir()
        ])

    }

    private void writeDeployitConf() {
        project.logger.lifecycle("Writing deployit.conf file")

        def configStream = StartConfigServerTask.class.classLoader.getResourceAsStream("central-conf/deployit.conf")
        def confDest = Paths.get("${ExtensionsUtil.getConfigServerWorkingDir(project)}/conf/deployit.conf")
        FileUtil.copyFile(configStream, confDest)
    }

    private void writeCentralConfig() {
        project.logger.lifecycle("Writing central-configuration.yaml file")
        def centralConfig = new File("${ExtensionsUtil.getConfigServerWorkingDir(project)}/conf/central-configuration.yaml")
        Files.copy(StartConfigServerTask.class.classLoader.getResourceAsStream("central-conf/central-configuration.yaml"), Paths.get(centralConfig.toURI()), StandardCopyOption.REPLACE_EXISTING)

        DumperOptions options = new DumperOptions()
        options.setPrettyFlow(true)
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
        Yaml parser = new Yaml(options)
        def config = parser.load(centralConfig.text)
        config['server']['port'] = ExtensionsUtil.getExtension(project).configServerHttpPort

        centralConfig.text = parser.dump(config)
    }

    private void waitForBoot() {
        def extension = ExtensionsUtil.getExtension(project)
        project.logger.lifecycle("Waiting for config server to start on port ${extension.configServerHttpPort}")
        int triesLeft = extension.serverPingTotalTries
        boolean success = false
        while (triesLeft > 0 && !success) {
            try {
                def http = HTTPUtil.buildRequest("http://localhost:${extension.configServerHttpPort}/actuator/")
                http.get([:]) { resp, reader ->
                    println("Config Server successfully started on port ${extension.configServerHttpPort}")
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
            throw new GradleException("Config Server failed to start")
        }
    }

    @TaskAction
    void launch() {
        ShutDownConfigServerTask.shutdownServer(project)
        writeDeployitConf()
        writeCentralConfig()
        startConfigServer()
        waitForBoot()
    }
}