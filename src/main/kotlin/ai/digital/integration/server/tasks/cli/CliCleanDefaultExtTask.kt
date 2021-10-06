package ai.digital.integration.server.tasks.cli

import ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.util.CliUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class CliCleanDefaultExtTask : DefaultTask() {

    init {
        this.group = PLUGIN_GROUP
        this.mustRunAfter(DownloadAndExtractCliDistTask.NAME)
    }

    companion object {
        @JvmStatic
        val NAME = "cliCleanDefaultExt"
    }

    @TaskAction
    fun launch() {
        if (CliUtil.getCli(project).cleanDefaultExtContent) {
            project.logger.lifecycle("Removing all default content in CLI ext folder.")

            val folder = CliUtil.getCliExtFolder(project)
            project.file(folder).walkTopDown().forEach { fileName ->
                project.logger.lifecycle("Removing ${folder}/${fileName}.")
                project.delete("${folder}/${fileName}")
            }
        }
    }
}
