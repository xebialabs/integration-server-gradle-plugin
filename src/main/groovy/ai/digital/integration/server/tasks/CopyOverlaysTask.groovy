package ai.digital.integration.server.tasks

import ai.digital.integration.server.domain.Server
import ai.digital.integration.server.util.*
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Copy

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class CopyOverlaysTask extends DefaultTask {
    static NAME = "copyOverlays"

    static LIB_KEY = "lib"

    CopyOverlaysTask() {
        def dependencies = [
                DeletePrepackagedXldStitchCoreTask.NAME,
                ServerUtil.getServerInstallTaskName(project)
        ]
        this.configure { ->
            group = PLUGIN_GROUP
            dependsOn(dependencies)

            finalizedBy CheckUILibVersionsTask.NAME
            project.afterEvaluate {
                Server server = ServerUtil.getServer(project)
                project.logger.lifecycle("Copying overlays on Deploy server ${server.name}")

                addDatabaseDependency(project, server)
                addMqDependency(project, server)

                server.overlays.each { Map.Entry<String, List<Object>> definition ->
                    def configurationName = "${ExtensionUtil.EXTENSION_NAME}${definition.key.capitalize().replace("/", "")}"
                    def config = project.buildscript.configurations.create(configurationName)
                    definition.value.each { dependencyNotation ->
                        project.buildscript.dependencies.add(configurationName, dependencyNotation)
                    }

                    def task = project.getTasks().register("copy${configurationName.capitalize()}", Copy.class, new Action<Copy>() {
                        @Override
                        void execute(Copy copy) {
                            config.files.each { File file ->
                                copy.from { shouldUnzip(file) ? project.zipTree(file) : file }
                            }
                            copy.into { "${ServerUtil.getServerWorkingDir(project)}/${definition.key}" }
                        }
                    })
                    project.tasks.getByName(task.name).dependsOn ServerUtil.getServerInstallTaskName(project)
                    this.dependsOn task
                }
            }
        }
    }

    private static boolean shouldUnzip(File file) {
        file.name.endsWith(".zip")
    }

    private static def overlayDependency(String version, Project project, Server server, List<Object> libOverlays, Object dependency) {
        if (version != null && !version.isEmpty()) {
            if (server.runtimeDirectory != null) {
                def configuration = project.getConfigurations().getByName(ConfigurationsUtil.DEPLOY_SERVER)
                configuration.dependencies.add(
                        project.dependencies.create("${dependency.driverDependency}:${version}")
                )
            }
            libOverlays.add("${dependency.driverDependency}:${version}")
            server.overlays.put(LIB_KEY, libOverlays)
        }
    }

    private static def addDatabaseDependency(Project project, Server server) {
        def dbname = DbUtil.databaseName(project)
        def dbDependencies = DbUtil.detectDbDependencies(dbname)
        def libOverlay = server.overlays.getOrDefault(LIB_KEY, new ArrayList<Object>())
        def version = DbUtil.getDatabase(project).driverVersions[dbname]

        overlayDependency(version, project, server, libOverlay, dbDependencies)
    }

    private static def addMqDependency(Project project, Server server) {
        def mqName = MqUtil.mqName(project)
        def mqDependency = MqUtil.detectMqDependency(mqName)
        def ext = ExtensionUtil.getExtension(project)
        def libOverlay = server.overlays.getOrDefault(LIB_KEY, new ArrayList<Object>())
        String version = ext.mqDriverVersions[mqName]

        overlayDependency(version, project, server, libOverlay, mqDependency)
    }

}
