package ai.digital.integration.server.deploy.tasks.cli

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.deploy.domain.Cli
import ai.digital.integration.server.deploy.internals.CliUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.tasks.tls.TlsApplicationConfigurationOverrideTask
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

abstract class RunCliTask : DefaultTask() {

    @get:Input
    @get:Option(option = "security", description = "Use true when TLS is enabled on the server side.")
    @get:Optional
    abstract val secure: Property<Boolean>


    @get:Input
    @get:Option(option = "files ", description = "Additional files to Execute.")
    @get:Optional
    var filesToExec: List<File> = mutableListOf()

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
        val filesExec = if (filesToExec.isNotEmpty()) filesToExec else cli.filesToExecute
        CliUtil.executeScripts(project, filesExec, "cli", secure.getOrElse(false))
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
