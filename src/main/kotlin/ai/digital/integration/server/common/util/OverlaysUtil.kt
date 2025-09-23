package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.domain.api.Container
import ai.digital.integration.server.common.domain.api.DriverDependencyAware
import ai.digital.integration.server.deploy.internals.DeployConfigurationsUtil
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import java.io.File
import java.util.*

class OverlaysUtil {
    companion object {
        private const val HOTFIX_LIB_KEY = "hotfix/lib"

        private fun shouldUnzip(file: File): Boolean {
            return file.name.endsWith(".zip")
        }

        fun defineOverlay(
            project: Project,
            currentTask: Task,
            workingDir: String,
            prefix: String,
            overlay: Map.Entry<String, List<*>>,
            dependedTasks: List<String>,
            customPrefix: String? = null
        ) {
            val configurationName = if (customPrefix != null) {
                "${customPrefix}${prefix}${overlay.key.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }.replace("/", "")}"
            } else {
                "${prefix}${overlay.key.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }.replace("/", "")}"
            }
            val config = project.configurations.create(configurationName)
            overlay.value.forEach { dependencyNotation ->
                project.buildscript.dependencies.add(configurationName, dependencyNotation as Any)
            }
            val excludeTransientDep  = listOf(
                    "org.slf4j:slf4j-api",
                    "jakarta.jms:jakarta.jms-api"
            )
            for (exclusion in excludeTransientDep) {
                val (group, module) = exclusion.split(':')
                project.logger.lifecycle("Excluding overlays transient dependencies in Deploy server - Group: $group, Module: $module")
                config.exclude(mapOf("group" to group, "module" to module))
            }

            val copyTask =
                project.tasks.register("copy${configurationName.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }}", Copy::class.java) {
                    config.files.forEach { file ->
                        from(if (shouldUnzip(file)) project.zipTree(file) else file)
                    }
                    into("${workingDir}/${overlay.key}")
                }
            if (dependedTasks.isNotEmpty()) {
                project.tasks.getByName(copyTask.name).dependsOn(dependedTasks)
            }
            currentTask.dependsOn(copyTask)
        }

        private fun overlayDependency(
            project: Project,
            version: String?,
            container: Container,
            libOverlays: MutableList<Any>,
            dependency: DriverDependencyAware
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

        fun addDatabaseDependency(project: Project, container: Container) {
            val dbname = DbUtil.databaseName(project)
            val dbDependencies = DbUtil.detectDbDependencies(dbname)
            val libOverlay = container.overlays.getOrDefault(HOTFIX_LIB_KEY, mutableListOf())
            val version = DbUtil.getDatabase(project).driverVersions[dbname]

            overlayDependency(project, version, container, libOverlay, dbDependencies)
        }

        fun addMqDependency(project: Project, container: Container) {
            val mqName = MqUtil.mqName(project)
            val mqDependency = MqUtil.detectMqDependency(mqName)
            val ext = DeployExtensionUtil.getExtension(project)
            val libOverlay = container.overlays.getOrDefault(HOTFIX_LIB_KEY, mutableListOf())
            val version = ext.mqDriverVersions[mqName]

            overlayDependency(project, version, container, libOverlay, mqDependency)
        }

        fun addCacheDependency(project: Project, container: Container) {
            val cacheDependency = CacheUtil.getCacheDependency()
            val libOverlay = container.overlays.getOrDefault(HOTFIX_LIB_KEY, mutableListOf())
            val version = CacheUtil.getCacheDependencyVersion(CacheUtil.getCacheProviderName())

            overlayDependency(project, version, container, libOverlay, cacheDependency)
        }
    }

}
