package ai.digital.integration.server.deploy.tasks.cli

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.deploy.internals.CliUtil
import ai.digital.integration.server.common.util.OverlaysUtil
import org.gradle.api.DefaultTask

open class CliOverlaysTask : DefaultTask() {

    companion object {
        const val NAME = "cliOverlays"
        const val PREFIX = "cli"
    }

    init {
        this.group = PLUGIN_GROUP
        this.mustRunAfter(DownloadAndExtractCliDistTask.NAME)
        this.mustRunAfter(CliCleanDefaultExtTask.NAME)
        this.mustRunAfter(CopyCliBuildArtifactsTask.NAME)

        project.afterEvaluate {
            CliUtil.getCli(project).overlays.get().forEach { overlay ->
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
