package com.xebialabs.gradle.integration.tasks.pluginManager

import com.xebialabs.gradle.integration.tasks.StartIntegrationServerTask
import com.xebialabs.gradle.integration.util.EnvironmentUtil
import com.xebialabs.gradle.integration.util.ExtensionsUtil
import com.xebialabs.gradle.integration.util.ProcessUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

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
        Paths.get(ExtensionsUtil.getServerWorkingDir(project), "bin").toFile()
    }


    private void startServer() {
        project.logger.lifecycle("Launching server")
        ProcessUtil.exec([
                command    : "run",
                params     : ["plugin-manager-cli"],
                environment: EnvironmentUtil.getEnv(project, "DEPLOYIT_SERVER_OPTS"),
                workDir    : getBinDir(),
                inheritIO  : true
        ])
    }

    @TaskAction
    void launch() {
        startServer()
    }
}
