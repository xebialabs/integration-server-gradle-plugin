package ai.digital.integration.server.tasks.cli

import ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.util.CliUtil
import ai.digital.integration.server.util.ConfigurationsUtil.Companion.SERVER_CLI_DIST
import ai.digital.integration.server.util.IntegrationServerUtil
import ai.digital.integration.server.util.TestUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Copy

abstract class DownloadAndExtractCliDistTask : DefaultTask() {

    init {
        this.group = PLUGIN_GROUP

        if (CliUtil.hasCli(project) || TestUtil.hasTests(project)) {
            val version = CliUtil.getCli(project).version
            project.logger.lifecycle("Downloading and extracting the CLI ${version}.")

            project.buildscript.dependencies.add(
                SERVER_CLI_DIST,
                "ai.digital.deploy:deploy-cli:${version}@zip"
            )

            val taskName = "${NAME}download"
            this.dependsOn(project.tasks.register(taskName, Copy::class.java) { copy ->
                copy.from(project.zipTree(project.buildscript.configurations.getByName(SERVER_CLI_DIST).singleFile))
                copy.into(IntegrationServerUtil.getDist(project))
            })
        }
    }

    companion object {
        @JvmStatic
        val NAME = "downloadAndExtractCli"
    }
}
