package com.xebialabs.gradle.integration.tasks.satellite

import com.xebialabs.gradle.integration.tasks.CheckUILibVersionsTask
import com.xebialabs.gradle.integration.util.ExtensionsUtil
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Copy

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class CopySatelliteOverlaysTask extends DefaultTask {
    static NAME = "copySatelliteOverlays"

    static boolean shouldUnzip(File file) {
        file.name.endsWith(".zip")
    }

    CopySatelliteOverlaysTask() {
        this.configure { ->
            group = PLUGIN_GROUP
            mustRunAfter DownloadAndExtractSatelliteDistTask.NAME
            finalizedBy CheckUILibVersionsTask.NAME
            project.afterEvaluate {

                ExtensionsUtil.getExtension(project).satelliteOverlays.each { definition ->
                    def configurationName = "satelliteServer${definition.key.capitalize().replace("/", "")}"
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
                            copy.into { "${ExtensionsUtil.getSatelliteWorkingDir(project)}/${definition.key}" }
                        }
                    })
                    project.tasks.getByName(task.name).dependsOn DownloadAndExtractSatelliteDistTask.NAME
                    this.dependsOn task
                }
            }
        }
    }
}
