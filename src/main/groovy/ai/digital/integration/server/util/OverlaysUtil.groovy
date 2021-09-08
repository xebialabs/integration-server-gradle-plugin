package ai.digital.integration.server.util

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy

class OverlaysUtil {

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

}
