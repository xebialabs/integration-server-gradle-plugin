package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.IntegrationServerExtension
import com.xebialabs.gradle.integration.util.ConfigurationsUtil
import com.xebialabs.gradle.integration.util.DbUtil
import com.xebialabs.gradle.integration.util.ExtensionsUtil
import com.xebialabs.gradle.integration.util.MqUtil
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Copy

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class CopyOverlaysTask extends DefaultTask {
    static LIB_KEY = "lib"
    static NAME = "copyOverlays"

    static boolean shouldUnzip(File file) {
        file.name.endsWith(".zip")
    }

    private static def overlayDependency(String version, IntegrationServerExtension ext, Project project, Object libOverlay, Object dependency) {
        if (version != null && !version.isEmpty()) {
            if (ext.serverRuntimeDirectory != null) {
                def configuration = project.getConfigurations().getByName(ConfigurationsUtil.INTEGRATION_TEST_SERVER)
                configuration.dependencies.add(
                        project.dependencies.create("${dependency.driverDependency}:${version}")
                )
            }
            libOverlay.add("${dbDependency.driverDependency}:${version}")
            ext.overlays.put(LIB_KEY, libOverlay)
        }
    }

    private static def addDatabaseDependency(Project project) {
        def dbname = DbUtil.databaseName(project)
        def dbDependency = DbUtil.detectDbDependency(dbname)
        def ext = ExtensionsUtil.getExtension(project)
        def libOverlay = ext.overlays.getOrDefault(LIB_KEY, new ArrayList<Object>())
        def version = ext.driverVersions[dbname]

        overlayDependency(version, ext, project, libOverlay, dbDependency)
    }

    private static def addMqDependency(project) {
        def mqname = MqUtil.mqName(project)
        def mqDependency = MqUtil.detectMqDependency(mqname)
        def ext = ExtensionsUtil.getExtension(project)
        def libOverlay = ext.overlays.getOrDefault(LIB_KEY, new ArrayList<Object>())
        def version = ext.mqDriverVersions[mqname]

        overlayDependency(version, ext, project, libOverlay, mqDependency)
    }


    CopyOverlaysTask() {
        this.configure { ->
            group = PLUGIN_GROUP
            mustRunAfter DownloadAndExtractServerDistTask.NAME
            mustRunAfter DeletePrepackagedXldStitchCoreTask.NAME
            finalizedBy CheckUILibVersionsTask.NAME
            project.afterEvaluate {
                addDatabaseDependency(project)
                addMqDependency(project)

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
