package ai.digital.integration.server.deploy.tasks.server

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.DeployServerUtil.Companion.getServerWorkingDir
import ai.digital.integration.server.deploy.internals.DeployServerUtil.Companion.isDockerBased
import de.vandermeer.asciitable.AsciiTable
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment
import groovy.json.JsonSlurper
import org.apache.commons.io.IOUtils

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.closureOf
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

@Suppress("UNCHECKED_CAST")
open class CheckUILibVersionsTask : DefaultTask() {

    companion object {
        const val NAME = "checkUILibVersions"
    }

    init {
        this.group = PluginConstant.PLUGIN_GROUP
        val dependencies = listOf(ServerCopyOverlaysTask.NAME, CopyServerBuildArtifactsTask.NAME)

        this.configure(closureOf<CheckUILibVersionsTask> {
            dependsOn(dependencies)
            onlyIf(closureOf<CheckUILibVersionsTask> {
                !isDockerBased(project)
            })
        })
    }

    private fun parseVersions(stream: ZipInputStream): Any? {
        val jsonSlurper = JsonSlurper()
        return jsonSlurper.parse(IOUtils.toByteArray(stream))
    }

    private fun parsePluginName(stream: ZipInputStream): String? {
        val prefix = "plugin="
        return IOUtils.toString(stream, Charset.defaultCharset())
            .split("\n")
            .find {
                it.startsWith(prefix)
            }?.substring(prefix.length)
    }

    private fun extractPluginMetadata(xldpZip: ZipFile, internalJarEntry: ZipEntry): Map<String, Any?> {
        val zipStream = ZipInputStream(xldpZip.getInputStream(internalJarEntry))
        zipStream.use { stream ->
            var entry = stream.nextEntry
            var pluginName: Any? = null
            var versions: Any? = null
            while (entry != null && (pluginName == null || versions == null)) {
                if (entry.name.endsWith("-metadata.json")) {
                    versions = parseVersions(stream)
                }
                if (entry.name == "plugin-version.properties") {
                    pluginName = parsePluginName(stream)
                }
                entry = stream.nextEntry
            }

            return if (pluginName != null && versions != null)
                mapOf(
                    "plugin" to pluginName,
                    "versions" to versions
                )
            else
                mapOf()
        }
    }

    private fun checkForMismatch(libName: String, plugins: List<Map<String, Any?>>): List<Map<String, Any?>>? {
        val pluginVersions: List<Map<String, Any?>> = plugins.map { plugin ->
            val version = (plugin["versions"] as Map<String, Any?>)[libName]
            if (version != null)
                mapOf(
                    "plugin" to plugin["plugin"],
                    "version" to version
                )
            else
                mapOf()
        }

        val mismatchSet = pluginVersions.flatMap { current ->
            val version = current["version"] as String
            setOf(version)
        }.toSet()

        return if (mismatchSet.size > 1)
            pluginVersions
        else
            null
    }

    private fun collectPluginMetadata(project: Project, files: List<File>): List<Map<String, Any?>> {
        project.logger.lifecycle("Collecting plugins metadata")

        return files.filter { file ->
            file.name.endsWith(".xldp")
        }.map { plugin ->
            project.logger.lifecycle("Extracting plugin's metadata from the plugin $plugin")
            if (plugin.length() > 0) {
                val xldpZip = ZipFile(plugin)
                val internalJarEntry = xldpZip.entries()
                    .toList()
                    .find {
                        it.name.endsWith(".jar")
                    }
                if (internalJarEntry != null)
                    extractPluginMetadata(xldpZip, internalJarEntry)
                else
                    mapOf()
            } else {
                project.logger.lifecycle("Skipping the check of $plugin as the content is empty.")
                mapOf()
            }
        }.filter { map ->
            map.isNotEmpty()
        }
    }

    private fun findMismatches(metadata: List<Map<String, Any?>>): List<Map<String, Any?>> {
        val allLibs: Set<String> = metadata.flatMap { map ->
            (map["versions"] as Map<String, Any?>).keys
        }.toSet()


        return allLibs.map { lib ->
            val mismatch = checkForMismatch(lib, metadata)
            if (mismatch != null)
                mapOf(
                    "lib" to lib,
                    "versions" to mismatch
                )
            else
                mapOf()
        }.filter { map -> map.isNotEmpty() }
    }

    private fun formatErrorMessage(mismatches: List<Map<String, Any?>>): String {
        val table = AsciiTable()
        table.addRule()
        table.addRow(null, "Version(s) mismatch has been detected").setTextAlignment(TextAlignment.CENTER)
        table.addRule()

        mismatches.forEach { current ->
            table.addRow(null, current["lib"]).setTextAlignment(TextAlignment.CENTER)
            table.addRule()
            val versions = current["versions"] as List<Map<String, Any?>>
            versions.forEach { descriptor ->
                table.addRow(descriptor["plugin"], descriptor["version"])
                table.addRule()
            }
        }
        return "/n" + table.render()
    }

    @TaskAction
    fun check() {
        project.logger.lifecycle("Checking UI Lib Versions on Deploy server")
        val plugins = Paths.get(getServerWorkingDir(project))
            .resolve("plugins")
            .resolve("xld-official")
            .toFile()
            .listFiles()
        if (plugins != null) {
            val metadata = collectPluginMetadata(project, plugins.toList())
            val mismatches = findMismatches(metadata)
            if (mismatches.isNotEmpty()) {
                throw GradleException(formatErrorMessage(mismatches))
            }
        } else {
            /**
             * This can happen when Deploy started from runtime directory.
             * Extra logic has to be added to solve this kind of setup.
             */
            project.logger.lifecycle("No plugins have been found on Deploy Server. Skipping checking versions for UI libraries.")
        }
    }


}
