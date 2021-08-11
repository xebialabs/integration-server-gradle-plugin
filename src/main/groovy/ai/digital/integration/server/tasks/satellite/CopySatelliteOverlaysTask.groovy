package ai.digital.integration.server.tasks.satellite

import ai.digital.integration.server.domain.Satellite
import ai.digital.integration.server.util.SatelliteUtil
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Copy

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

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