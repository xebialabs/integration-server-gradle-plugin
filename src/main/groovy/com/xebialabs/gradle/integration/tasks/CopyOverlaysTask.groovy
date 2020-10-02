package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.util.DbUtil
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

    private static def addDatabaseDependency(project) {
        def libKey = 'lib'
        def dbname = DbUtil.databaseName(project)
        def dbDependency = DbUtil.detectDbDependency(dbname)
        def ext = ExtensionsUtil.getExtension(project)
        def libOverlay = ext.overlays.getOrDefault(libKey, new ArrayList<Object>())
        def version = ext.driverVersions[dbname]
        if (version != null && !version.isEmpty()) {
            libOverlay.add("${dbDependency.driverDependency}:${version}")
            ext.overlays.put(libKey, libOverlay)
        }
    }

    CopyOverlaysTask() {
        this.configure { ->
            group = PLUGIN_GROUP
            mustRunAfter DownloadAndExtractServerDistTask.NAME
            mustRunAfter DeletePrepackagedXldStitchCoreTask.NAME
            finalizedBy CheckUILibVersionsTask.NAME
            project.afterEvaluate {
                addDatabaseDependency(project)

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
