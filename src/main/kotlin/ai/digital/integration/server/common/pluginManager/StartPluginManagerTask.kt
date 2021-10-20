package ai.digital.integration.server.common.pluginManager

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.deploy.tasks.server.StartServerInstanceTask
import ai.digital.integration.server.deploy.tasks.tls.TlsApplicationConfigurationOverrideTask
import ai.digital.integration.server.deploy.util.DeployServerUtil
import ai.digital.integration.server.deploy.util.EnvironmentUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Paths

open class StartPluginManagerTask : DefaultTask() {

    init {
        val dependencies = mutableListOf(StartServerInstanceTask.NAME)

        if (DeployServerUtil.isTls(project)) {
            dependencies.add(TlsApplicationConfigurationOverrideTask.NAME)
        }

        this.dependsOn(dependencies)
        this.group = PLUGIN_GROUP
        this.onlyIf { !DeployServerUtil.isDockerBased(project) }
    }

    private fun getBinDir(): File {
        return Paths.get(DeployServerUtil.getServerWorkingDir(project), "bin").toFile()
    }

    private fun startPluginManager(server: Server) {
        val process = ProcessUtil.exec(
            mapOf(
                "command" to "run",
                "discardIO" to true,
                "environment" to EnvironmentUtil.getServerEnv(project, server),
                "params" to arrayOf("plugin-manager-cli"),
                "workDir" to getBinDir()
            )
        )
        project.logger.lifecycle(
            "Launched Plugin Manager on Deploy server $server.name on PID [${process.pid()}] with command [${
                process.info().commandLine().orElse("")
            }]."
        )
    }

    @TaskAction
    fun launch() {
        val server = DeployServerUtil.getServer(project)
        project.logger.lifecycle("Launching Plugin Manager on Deploy server $server.name")
        startPluginManager(server)
    }

    companion object {
        const val NAME = "startPluginManager"
    }
}
