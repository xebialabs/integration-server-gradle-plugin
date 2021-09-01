package ai.digital.integration.server.tasks.satellite

import ai.digital.integration.server.domain.Satellite
import ai.digital.integration.server.util.SatelliteUtil
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Copy

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class SatelliteOverlaysTask extends DefaultTask {
    static NAME = "satelliteOverlays"

    static PREFIX = "satellite"

    static boolean shouldUnzip(File file) {
        file.name.endsWith(".zip")
    }

    SatelliteOverlaysTask() {
        this.configure { ->
            group = PLUGIN_GROUP
            mustRunAfter DownloadAndExtractSatelliteDistTask.NAME

            project.afterEvaluate {
                SatelliteUtil.getSatellites(project).each { Satellite satellite ->
                    satellite.overlays.each { Map.Entry<String, List<Object>> overlay ->
                        def configurationName = "${PREFIX}${overlay.key.capitalize().replace("/", "")}"
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
                        project.tasks.getByName(task.name).dependsOn "downloadAndExtractSatellite${satellite.name}"
                        this.dependsOn task
                    }
                }
            }
        }
    }
}
