package ai.digital.integration.server.tasks.cli


import ai.digital.integration.server.util.CliUtil
import ai.digital.integration.server.util.OverlaysUtil
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Copy

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class CliOverlaysTask extends DefaultTask {
    public static String NAME = "cliOverlays"
    public static String PREFIX = "cli"

    CliOverlaysTask() {
        this.configure { ->
            group = PLUGIN_GROUP
            mustRunAfter DownloadAndExtractCliDistTask.NAME
            mustRunAfter CliCleanDefaultExtTask.NAME
            mustRunAfter CopyCliBuildArtifactsTask.NAME

            project.afterEvaluate {
                CliUtil.getCli(project).overlays.each { Map.Entry<String, List<Object>> overlay ->
                    OverlaysUtil.defineOverlay(project, this, CliUtil.getWorkingDir(project), PREFIX, overlay, [DownloadAndExtractCliDistTask.NAME])
                }
            }
        }
    }
}
