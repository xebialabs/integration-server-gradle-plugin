package com.xebialabs.gradle.integration.tasks.pluginManager

import com.xebialabs.gradle.integration.domain.Server
import com.xebialabs.gradle.integration.tasks.StartIntegrationServerTask
import com.xebialabs.gradle.integration.util.EnvironmentUtil
import com.xebialabs.gradle.integration.util.LocationUtil
import com.xebialabs.gradle.integration.util.ProcessUtil
import com.xebialabs.gradle.integration.util.ServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

import static com.xebialabs.gradle.integration.constant.PluginConstant.PLUGIN_GROUP

class StartPluginManagerTask extends DefaultTask {
    static NAME = "StartPluginManager"

    StartPluginManagerTask() {
        def dependencies = [
                StartIntegrationServerTask.NAME
        ]

        this.configure {
            group = PLUGIN_GROUP
            dependsOn(dependencies)
        }
    }

    private def getBinDir() {
        Paths.get(LocationUtil.getServerWorkingDir(project), "bin").toFile()
    }

    private void startPluginManager(Server server) {
        ProcessUtil.exec([
                command    : "run",
                params     : ["plugin-manager-cli"],
                environment: EnvironmentUtil.getServerEnv(server),
                workDir    : getBinDir(),
                inheritIO  : true
        ])
    }

    @TaskAction
    void launch() {
        def server = ServerUtil.getServer(project)
        project.logger.lifecycle("Launching Plugin Manager on Deploy server $server.name")
        startPluginManager(server)
    }
}
