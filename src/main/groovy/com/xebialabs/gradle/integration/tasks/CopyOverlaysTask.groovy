package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.util.ExtensionsUtil
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Copy

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class CopyOverlaysTask extends DefaultTask {
    static NAME = "copyOverlays"

    static boolean shouldUnzip(File file) {
        file.name.endsWith(".zip")
    }

    CopyOverlaysTask() {
        this.configure { ->
            group = PLUGIN_GROUP
            mustRunAfter DownloadAndExtractServerDistTask.NAME
            finalizedBy CheckUILibVersionsTask.NAME
            project.afterEvaluate {
                ExtensionsUtil.getExtension(project).overlays.each { definition ->
                    def configurationName = "integrationServer${definition.key.capitalize().replace("/", "")}"
                    def config = project.buildscript.configurations.create(configurationName)
                    definition.value.each { dependencyNotation ->
                        project.buildscript.dependencies.add(configurationName, dependencyNotation)
                    }

                    def task = project.getTasks().register("copy${configurationName.capitalize()}", Copy.class, new Action<Copy>() {
                        @Override
                        void execute(Copy copy) {
                            config.files.each { file ->
                                copy.from { shouldUnzip(file) ? project.zipTree(file) : file }
                            }
                            copy.into { "${ExtensionsUtil.getServerWorkingDir(project)}/${definition.key}" }
                        }
                    })
                    project.tasks.getByName(task.name).dependsOn DownloadAndExtractServerDistTask.NAME
                    this.dependsOn task
                }
            }
        }
    }
}
