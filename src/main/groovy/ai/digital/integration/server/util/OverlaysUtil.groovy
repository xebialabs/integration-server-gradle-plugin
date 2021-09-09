package ai.digital.integration.server.util


import ai.digital.integration.server.domain.api.Engine
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy

class OverlaysUtil {

    // We locate libraries to hotfix/lib, as "lib" is not mounted in a docker setup.
    static HOTFIX_LIB_KEY = "hotfix/lib"

    private static boolean shouldUnzip(File file) {
        file.name.endsWith(".zip")
    }

    static def defineOverlay(Project project, Task currentTask, String workingDir, String prefix, Map.Entry<String, List<Object>> overlay,
                             List<String> dependedTasks) {
        def configurationName = "${prefix}${overlay.key.capitalize().replace("/", "")}"
        def config = project.buildscript.configurations.create(configurationName)
        overlay.value.each { dependencyNotation ->
            project.buildscript.dependencies.add(configurationName, dependencyNotation)
        }

        def copyTask = project.getTasks().register("copy${configurationName.capitalize()}", Copy.class, new Action<Copy>() {
            @Override
            void execute(Copy copy) {
                config.files.each { file ->
                    copy.from { shouldUnzip(file) ? project.zipTree(file) : file }
                }
                copy.into { "${workingDir}/${overlay.key}" }
            }
        })
        if (!dependedTasks.empty) {
            project.tasks.getByName(copyTask.name).dependsOn dependedTasks
        }
        currentTask.dependsOn copyTask
    }

    private static def overlayDependency(String version, Project project, Engine engine, List<Object> libOverlays, Object dependency) {
        if (version != null && !version.isEmpty()) {
            if (engine.runtimeDirectory != null) {
                def configuration = project.getConfigurations().getByName(ConfigurationsUtil.DEPLOY_SERVER)
                configuration.dependencies.add(
                        project.dependencies.create("${dependency.driverDependency}:${version}")
                )
            }
            libOverlays.add("${dependency.driverDependency}:${version}")
            engine.overlays.put(HOTFIX_LIB_KEY, libOverlays)
        }
    }

    static def addDatabaseDependency(Project project, Engine engine) {
        def dbname = DbUtil.databaseName(project)
        def dbDependencies = DbUtil.detectDbDependencies(dbname)
        def libOverlay = engine.overlays.getOrDefault(HOTFIX_LIB_KEY, new ArrayList<Object>())
        def version = DbUtil.getDatabase(project).driverVersions[dbname]

        overlayDependency(version, project, engine, libOverlay, dbDependencies)
    }

    static def addMqDependency(Project project, Engine engine) {
        def mqName = MqUtil.mqName(project)
        def mqDependency = MqUtil.detectMqDependency(mqName)
        def ext = ExtensionUtil.getExtension(project)
        def libOverlay = engine.overlays.getOrDefault(HOTFIX_LIB_KEY, new ArrayList<Object>())
        String version = ext.mqDriverVersions[mqName]

        overlayDependency(version, project, engine, libOverlay, mqDependency)
    }

}
