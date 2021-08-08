package com.xebialabs.gradle.integration.tasks.satellite

import com.xebialabs.gradle.integration.domain.Satellite
import com.xebialabs.gradle.integration.util.SatelliteUtil
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Copy

import static com.xebialabs.gradle.integration.constant.PluginConstant.PLUGIN_GROUP

class CopySatelliteOverlaysTask extends DefaultTask {
    static NAME = "copySatelliteOverlays"

    static boolean shouldUnzip(File file) {
        file.name.endsWith(".zip")
    }

    CopySatelliteOverlaysTask() {
        this.configure { ->
            group = PLUGIN_GROUP
            mustRunAfter DownloadAndExtractSatelliteDistTask.NAME
            project.afterEvaluate {
                SatelliteUtil.getSatellites(project).each { Satellite satellite ->
                    satellite.overlays.each { Map.Entry<String, List<Object>> overlay ->
                        project.logger.lifecycle("Applying overlay for satellite ${satellite.name}.")

                        def configurationName = "satelliteServer${overlay.key.capitalize().replace("/", "")}"
                        def config = project.buildscript.configurations.create(configurationName)
                        overlay.value.each { dependencyNotation ->
                            project.buildscript.dependencies.add(configurationName, dependencyNotation)
                        }

                        def task = project.getTasks().register("copy${configurationName.capitalize()}", Copy.class, new Action<Copy>() {
                            @Override
                            void execute(Copy copy) {
                                config.files.each { file ->
                                    copy.from { shouldUnzip(file) ? project.zipTree(file) : file }
                                }
                                copy.into { "${SatelliteUtil.getSatelliteWorkingDir(project, satellite)}/${overlay.key}" }
                            }
                        })
                        project.tasks.getByName(task.name).dependsOn DownloadAndExtractSatelliteDistTask.NAME
                        this.dependsOn task
                    }
                }
            }
        }
    }
}
