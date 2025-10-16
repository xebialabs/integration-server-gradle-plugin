package ai.digital.integration.server.deploy.tasks.cli

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.OverlaysUtil
import ai.digital.integration.server.deploy.internals.CliUtil
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
        val currentTask = this

        val configureOverlays = {
            CliUtil.getCli(project).overlays.forEach { overlay ->
                OverlaysUtil.defineOverlay(project,
                    currentTask,
                    CliUtil.getWorkingDir(project),
                    PREFIX,
                    overlay,
                    listOf(DownloadAndExtractCliDistTask.NAME)
                )
            }
        }

        if (project.state.executed) {
            configureOverlays()
        } else {
            project.afterEvaluate {
                configureOverlays()
            }
        }
    }
}
