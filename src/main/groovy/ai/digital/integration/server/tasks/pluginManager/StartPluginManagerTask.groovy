package ai.digital.integration.server.tasks.pluginManager

import ai.digital.integration.server.domain.Server
import ai.digital.integration.server.tasks.StartIntegrationServerTask
import ai.digital.integration.server.util.EnvironmentUtil
import ai.digital.integration.server.util.LocationUtil
import ai.digital.integration.server.util.ProcessUtil
import ai.digital.integration.server.util.ServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

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
