package ai.digital.integration.server.tasks.cli

import ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.util.CliUtil
import ai.digital.integration.server.util.OverlaysUtil
import org.gradle.api.DefaultTask

abstract class CliOverlaysTask : DefaultTask() {

    companion object {
        @JvmStatic
        val NAME = "cliOverlays"

        @JvmStatic
        val PREFIX = "cli"
    }

    init {
        this.group = PLUGIN_GROUP
        this.mustRunAfter(DownloadAndExtractCliDistTask.NAME)
        this.mustRunAfter(CliCleanDefaultExtTask.NAME)
        this.mustRunAfter(CopyCliBuildArtifactsTask.NAME)

        project.afterEvaluate {
            CliUtil.getCli(project).overlays.forEach { overlay ->
                OverlaysUtil.defineOverlay(project,
                    this,
                    CliUtil.getWorkingDir(project),
                    PREFIX,
                    overlay,
                    listOf(DownloadAndExtractCliDistTask.NAME)
                )
            }
        }
    }
}
