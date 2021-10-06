package ai.digital.integration.server.tasks.cli

import ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.util.CliUtil
import ai.digital.integration.server.util.CopyBuildArtifactsUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class CopyCliBuildArtifactsTask : DefaultTask() {

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
        @JvmStatic
        val NAME = "copyCliBuildArtifacts"
    }
}
