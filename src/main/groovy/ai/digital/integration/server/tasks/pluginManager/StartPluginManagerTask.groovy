package ai.digital.integration.server.tasks.pluginManager

import ai.digital.integration.server.domain.Server
import ai.digital.integration.server.tasks.StartIntegrationServerTask
import ai.digital.integration.server.util.EnvironmentUtil
import ai.digital.integration.server.util.ProcessUtil
import ai.digital.integration.server.util.ServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class StartPluginManagerTask extends DefaultTask {
    static NAME = "startPluginManager"

    StartPluginManagerTask() {
        def dependencies = [
                StartIntegrationServerTask.NAME
        ]

        this.configure {
            group = PLUGIN_GROUP
            dependsOn(dependencies)
            onlyIf { !ServerUtil.isDockerBased(project) }
        }
    }

    private def getBinDir() {
        Paths.get(ServerUtil.getServerWorkingDir(project), "bin").toFile()
    }

    private void startPluginManager(Server server) {
        Process process = ProcessUtil.exec([
                command    : "run",
                discardIO  : true,
                environment: EnvironmentUtil.getServerEnv(server),
                params     : ["plugin-manager-cli"],
                workDir    : getBinDir(),
        ])
        project.logger.lifecycle("Launched Plugin Manager on Deploy server $server.name on PID [${process.pid()}] with command [${process.info().commandLine().orElse("")}].")
    }

    @TaskAction
    void launch() {
        def server = ServerUtil.getServer(project)
        project.logger.lifecycle("Launching Plugin Manager on Deploy server $server.name")
        startPluginManager(server)
    }
}
