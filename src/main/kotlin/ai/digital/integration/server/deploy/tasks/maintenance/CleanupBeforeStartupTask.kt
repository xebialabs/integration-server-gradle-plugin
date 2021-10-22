package ai.digital.integration.server.deploy.tasks.maintenance

import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class CleanupBeforeStartupTask : DefaultTask() {

    companion object {
        const val NAME = "cleanupBeforeStartup"
    }

    @TaskAction
    fun run() {
        project.logger.lifecycle("Clean up phase is about to get started.")
        DeployExtensionUtil.getExtension(project).maintenance.get().cleanupBeforeStartup.forEach { file ->
            if (file.exists()) {
                file.deleteRecursively()
                project.logger.lifecycle("$file has been removed.")
            }
        }
    }
}
