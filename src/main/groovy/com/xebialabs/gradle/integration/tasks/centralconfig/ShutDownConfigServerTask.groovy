package com.xebialabs.gradle.integration.tasks.centralconfig

import com.xebialabs.gradle.integration.util.ExtensionsUtil
import com.xebialabs.gradle.integration.util.HTTPUtil
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import java.util.concurrent.TimeUnit

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class ShutDownConfigServerTask extends DefaultTask {
    static NAME = "shutDownConfigServer"

    ShutDownConfigServerTask() {
        group = PLUGIN_GROUP
    }

    @TaskAction
    void shutdown() {
        shutdownServer(project)
    }

    static void shutdownServer(Project project) {
        def extension = ExtensionsUtil.getExtension(project)
        try {
            project.logger.lifecycle("Trying to shutdown Config server at port ${extension.configServerHttpPort}")
            def http = HTTPUtil.buildRequest("http://localhost:${extension.configServerHttpPort}/actuator/shutdown")
            http.post([:]) { resp, reader ->
                waitForShutdown(project)
                project.logger.lifecycle("Config server at port ${extension.configServerHttpPort} is now shutdown")
            }
        } catch (ignored) {
            project.logger.lifecycle("Config server at port ${extension.configServerHttpPort} is not running")
        }
    }

    private static void waitForShutdown(Project project) {
        def extension = ExtensionsUtil.getExtension(project)
        int triesLeft = extension.serverPingTotalTries
        boolean success = false
        while (triesLeft > 0 && !success) {
            try {
                def http = HTTPUtil.buildRequest("http://localhost:${extension.configServerHttpPort}/actuator/")
                http.handler.failure = {
                    project.logger.info("Config server successfully shutdown")
                    success = true
                }
                http.post([:]) { resp, reader ->
                    println("Waiting ${extension.serverPingRetrySleepTime} seconds for shutdown. ($triesLeft)")
                    TimeUnit.SECONDS.sleep(extension.serverPingRetrySleepTime)
                }
            } catch (ignored) {
                project.logger.info("Config server successfully shutdown.")
                success = true
                break
            }
        }
        if (!success) {
            throw new GradleException("Config Server failed to stop")
        }
    }
}
