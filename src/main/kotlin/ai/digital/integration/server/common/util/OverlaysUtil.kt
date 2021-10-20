package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.domain.api.DriverDependencyAware
import ai.digital.integration.server.common.domain.api.Container
import ai.digital.integration.server.deploy.util.DeployConfigurationsUtil
import ai.digital.integration.server.deploy.util.DeployExtensionUtil
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import java.io.File

class OverlaysUtil {
    companion object {
        @JvmStatic
        val HOTFIX_LIB_KEY = "hotfix/lib"

        private fun shouldUnzip(file: File): Boolean {
            return file.name.endsWith(".zip")
        }

        @JvmStatic
        fun defineOverlay(
                project: Project,
                currentTask: Task,
                workingDir: String,
                prefix: String,
                overlay: Map.Entry<String, List<Any>>,
                dependedTasks: List<String>,
                customPrefix: String? = null
        ) {
            val configurationName = if (customPrefix != null) {
                "${customPrefix}${prefix}${overlay.key.capitalize().replace("/", "")}"
            } else {
                "${prefix}${overlay.key.capitalize().replace("/", "")}"
            }
            val config = project.buildscript.configurations.create(configurationName)
            overlay.value.forEach { dependencyNotation ->
                project.buildscript.dependencies.add(configurationName, dependencyNotation)
            }

            val copyTask =
                project.tasks.register("copy${configurationName.capitalize()}", Copy::class.java) { copy ->
                    config.files.forEach { file ->
                        copy.from(if (shouldUnzip(file)) project.zipTree(file) else file)
                    }
                    copy.into("${workingDir}/${overlay.key}")
                }
            if (dependedTasks.isNotEmpty()) {
                project.tasks.getByName(copyTask.name).dependsOn(dependedTasks)
            }
            currentTask.dependsOn(copyTask)
        }

        @JvmStatic
        private fun overlayDependency(
            project: Project,
            version: String?,
            container: Container,
            libOverlays: MutableList<Any>,
            dependency: DriverDependencyAware,
        ) {
            if (version != null && version.isNotEmpty()) {
                if (container.runtimeDirectory != null) {
                    val configuration = project.configurations.getByName(DeployConfigurationsUtil.DEPLOY_SERVER)
                    configuration.dependencies.add(
                        project.dependencies.create("${dependency.driverDependency}:${version}")
                    )
                }
                libOverlays.add("${dependency.driverDependency}:${version}")
                container.overlays[HOTFIX_LIB_KEY] = libOverlays
            }
        }

        @JvmStatic
        fun addDatabaseDependency(project: Project, container: Container) {
            val dbname = DbUtil.databaseName(project)
            val dbDependencies = DbUtil.detectDbDependencies(dbname)
            val libOverlay = container.overlays.getOrDefault(HOTFIX_LIB_KEY, mutableListOf())
            val version = DbUtil.getDatabase(project).driverVersions[dbname]

            overlayDependency(project, version, container, libOverlay, dbDependencies)
        }

        @JvmStatic
        fun addMqDependency(project: Project, container: Container) {
            val mqName = MqUtil.mqName(project)
            val mqDependency = MqUtil.detectMqDependency(mqName)
            val ext = DeployExtensionUtil.getExtension(project)
            val libOverlay = container.overlays.getOrDefault(HOTFIX_LIB_KEY, mutableListOf())
            val version = ext.mqDriverVersions[mqName]

            overlayDependency(project, version, container, libOverlay, mqDependency)
        }
    }

}
