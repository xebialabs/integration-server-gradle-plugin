package com.xebialabs.gradle.integration.tasks.anonymizer

import com.xebialabs.gradle.integration.tasks.StartIntegrationServerTask
import com.xebialabs.gradle.integration.util.ExtensionsUtil
import com.xebialabs.gradle.integration.util.HTTPUtil
import com.xebialabs.gradle.integration.util.ProcessUtil
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class StartAnonymizerTask extends DefaultTask {
    static NAME = "StartAnonymizer"

    StartAnonymizerTask() {
        def dependencies = [
                StartIntegrationServerTask.NAME
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


    private void startServer() {
        project.logger.lifecycle("Launching server")
        ProcessUtil.exec([
            command    : "db-anonymizer.sh",
            environment: getEnv(),
            workDir    : getBinDir(),
            inheritIO  : true
        ])
    }

    private waitForAnonymizerGeneration(){
        def extension = ExtensionsUtil.getExtension(project)
        int triesLeft = extension.serverPingTotalTries
        boolean success = false
        while (triesLeft > 0 && !success) {
            try {
                 success =new File("${ExtensionsUtil.getConfigServerWorkingDir(project)}/xl-deploy-repository-dump.xml").exists()
            } catch (ignored) {
            }
            if (!success) {
                println("Waiting for ${extension.serverPingRetrySleepTime} second(s) before retry. ($triesLeft)")
                TimeUnit.SECONDS.sleep(extension.serverPingRetrySleepTime)
                triesLeft -= 1
            }
        }
        if (!success) {
            throw new GradleException("Anonymizer Generation failed to start")
        }
    }

    @TaskAction
    void launch() {
        startServer()
    }
}
