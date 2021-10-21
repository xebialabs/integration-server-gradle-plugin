package ai.digital.integration.server.deploy.tasks.cli

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.deploy.internals.CliUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class CliCleanDefaultExtTask : DefaultTask() {

    init {
        this.group = PLUGIN_GROUP
        this.mustRunAfter(DownloadAndExtractCliDistTask.NAME)
    }

    companion object {
        const val NAME = "cliCleanDefaultExt"
    }

    @TaskAction
    fun launch() {
        if (CliUtil.getCli(project).cleanDefaultExtContent.get()) {
            project.logger.lifecycle("Removing all default content in CLI ext folder.")

            val folder = CliUtil.getCliExtFolder(project)
            project.file(folder).walkTopDown().forEach { fileName ->
                project.logger.lifecycle("Removing ${folder}/${fileName}.")
                project.delete("${folder}/${fileName}")
            }
        }
    }
}
