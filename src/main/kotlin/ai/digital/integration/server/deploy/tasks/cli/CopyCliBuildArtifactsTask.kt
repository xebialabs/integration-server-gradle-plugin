package ai.digital.integration.server.deploy.tasks.cli

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.deploy.internals.CliUtil
import ai.digital.integration.server.common.util.CopyBuildArtifactsUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class CopyCliBuildArtifactsTask : DefaultTask() {

    init {
        this.group = PLUGIN_GROUP
        this.dependsOn(DownloadAndExtractCliDistTask.NAME)
    }

    @TaskAction
    fun launch() {
        val cli = CliUtil.getCli(project)
        CopyBuildArtifactsUtil.execute(project, cli.copyBuildArtifacts, CliUtil.getWorkingDir(project))
    }

    companion object {
        const val NAME = "copyCliBuildArtifacts"
    }
}
