package ai.digital.integration.server.deploy.tasks.cli

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.deploy.internals.CliUtil
import ai.digital.integration.server.deploy.internals.DeployConfigurationsUtil.Companion.SERVER_CLI_DIST
import ai.digital.integration.server.common.util.IntegrationServerUtil
import ai.digital.integration.server.common.util.TestUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Copy

open class DownloadAndExtractCliDistTask : DefaultTask() {

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
        const val NAME = "downloadAndExtractCli"
    }
}
