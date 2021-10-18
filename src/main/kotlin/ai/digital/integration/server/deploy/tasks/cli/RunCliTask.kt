package ai.digital.integration.server.deploy.tasks.cli

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.deploy.domain.Cli
import ai.digital.integration.server.deploy.tasks.TlsApplicationConfigurationOverrideTask
import ai.digital.integration.server.deploy.util.CliUtil
import ai.digital.integration.server.deploy.util.DeployServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

abstract class RunCliTask : DefaultTask() {

    @get:Input
    @get:Option(option = "security", description = "Use true when TLS is enabled on the server side.")
    @get:Optional
    abstract val secure: Property<Boolean>

    init {
        this.dependsOn(CliCleanDefaultExtTask.NAME)
        this.dependsOn(CliOverlaysTask.NAME)
        this.dependsOn(DownloadAndExtractCliDistTask.NAME)

        if (DeployServerUtil.isTls(project)) {
            this.dependsOn(TlsApplicationConfigurationOverrideTask.NAME)
        }

        this.group = PLUGIN_GROUP
    }

    private fun executeScripts(cli: Cli) {
        project.logger.lifecycle("Executing cli scripts ....")
        CliUtil.executeScripts(project, cli.filesToExecute, "cli", secure.getOrElse(false))
    }

    @TaskAction
    fun launch() {
        project.logger.lifecycle("Running a CLI provision script on the Deploy server.")
        executeScripts(CliUtil.getCli(project))
    }

    companion object {
        const val NAME = "runCli"
    }
}
