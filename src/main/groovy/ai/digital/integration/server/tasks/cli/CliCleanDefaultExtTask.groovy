package ai.digital.integration.server.tasks.cli

import ai.digital.integration.server.util.CliUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class CliCleanDefaultExtTask extends DefaultTask {
    public static String NAME = "cliCleanDefaultExt"

    CliCleanDefaultExtTask() {
        this.configure { ->
            group = PLUGIN_GROUP
            mustRunAfter DownloadAndExtractCliDistTask.NAME
        }
    }

    @TaskAction
    void launch() {
        if (CliUtil.getCli(project).cleanDefaultExtContent) {
            project.logger.lifecycle("Removing all default content in CLI ext folder.")

            def folder = CliUtil.getCliExtFolder(project)
            project.file(folder).list().each {
                String fileName ->
                    project.logger.lifecycle("Removing ${folder}/${fileName}.")
                    project.delete "${folder}/${fileName}"
            }
        }
    }
}
