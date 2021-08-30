package ai.digital.integration.server.tasks.cli


import ai.digital.integration.server.util.CliUtil
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Copy

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class CliOverlaysTask extends DefaultTask {
    static NAME = "cliOverlays"
    static PREFIX = "cli"

    static boolean shouldUnzip(File file) {
        file.name.endsWith(".zip")
    }

    CliOverlaysTask() {
        this.configure { ->
            group = PLUGIN_GROUP
            mustRunAfter DownloadAndExtractCliDistTask.NAME
            mustRunAfter CliCleanDefaultExtTask.NAME

            project.afterEvaluate {
                CliUtil.getCli(project).overlays.each { Map.Entry<String, List<Object>> overlay ->
                    def configurationName = "${PREFIX}${overlay.key.capitalize().replace("/", "")}"
                    def config = project.buildscript.configurations.create(configurationName)
                    overlay.value.each { dependencyNotation ->
                        project.buildscript.dependencies.add(configurationName, dependencyNotation)
                    }

                    def task = project.getTasks().register("copy${configurationName.capitalize()}", Copy.class, new Action<Copy>() {
                        @Override
                        void execute(Copy copy) {
                            config.files.each { File file ->
                                copy.from { shouldUnzip(file) ? project.zipTree(file) : file }
                            }
                            copy.into { "${CliUtil.getWorkingDir(project)}/${overlay.key}" }
                        }
                    })
                    project.tasks.getByName(task.name).dependsOn DownloadAndExtractCliDistTask.NAME
                    this.dependsOn task
                }
            }
        }
    }
}
