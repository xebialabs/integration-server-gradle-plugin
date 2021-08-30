package ai.digital.integration.server.tasks


import ai.digital.integration.server.util.ServerUtil
import de.vandermeer.asciitable.AsciiTable
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment
import groovy.json.JsonSlurper
import org.apache.commons.io.IOUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import java.nio.charset.Charset
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class CheckUILibVersionsTask extends DefaultTask {
    static NAME = "checkUILibVersions"

    CheckUILibVersionsTask() {
        def dependencies = [
                CopyOverlaysTask.NAME
        ]

        this.configure {
            group = PLUGIN_GROUP
            dependsOn(dependencies)
            onlyIf { !ServerUtil.isDockerBased(project) }
        }
    }

    private static def parseVersions(ZipInputStream stream) {
        def jsonSlurper = new JsonSlurper()
        jsonSlurper.parse(IOUtils.toByteArray(stream))
    }

    private static def parsePluginName(ZipInputStream stream) {
        def prefix = "plugin="
        IOUtils
                .toString(stream, Charset.defaultCharset())
                .split("\n")
                .find { it.startsWith(prefix) }
                .substring(prefix.length())
    }

    private static def extractPluginMetadata(ZipFile xldpZip, ZipEntry internalJarEntry) {
        def stream = new ZipInputStream(xldpZip.getInputStream(internalJarEntry))
        def entry = stream.nextEntry
        def pluginName
        def versions

        while (entry != null && (pluginName == null || versions == null)) {
            if (entry.name.endsWith("-metadata.json")) {
                versions = parseVersions(stream)
            }
            if (entry.name.equals("plugin-version.properties")) {
                pluginName = parsePluginName(stream)
            }
            entry = stream.nextEntry
        }
        try {
            pluginName != null && versions != null ? [[plugin: pluginName, versions: versions]] : []
        } finally {
            stream.close()
        }
    }

    private static def checkForMismatch(String libName, List<Map<String, Object>> plugins) {
        def pluginVersions = plugins.collectMany { plugin ->
            def version = plugin.versions.get(libName)
            version != null ? [[plugin: plugin.plugin, version: version]] : []
        }

        def mismatch = pluginVersions.inject(new HashSet<String>(), { acc, current ->
            acc.add(current.version as String)
            acc
        }).size() > 1
        mismatch ? pluginVersions : null
    }

    private static def collectPluginMetadata(Project project, List<File> files) {
        project.logger.lifecycle("Collecting plugins metadata")

        files.findAll { it.name.endsWith(".xldp") }.collectMany { File plugin ->
            project.logger.lifecycle("Extracting plugin's metadata from the plugin $plugin")
            if (plugin.size() > 0) {
                def xldpZip = new ZipFile(plugin)
                def internalJarEntry = xldpZip.entries().toList().find { it.name.endsWith(".jar") }
                internalJarEntry ? extractPluginMetadata(xldpZip, internalJarEntry) : []
            } else {
                project.logger.lifecycle("Skipping the check of $plugin as the content is empty.")
            }
        }
    }

    private static def findMismatches(List<Map<String, Object>> metadata) {
        def allLibs = metadata.inject(new HashSet<String>()) { acc, current -> acc.addAll(current.versions.keySet()); acc }
        allLibs.collectMany { lib ->
            def mismatch = checkForMismatch(lib, metadata)
            mismatch ? [[lib: lib, versions: mismatch]] : []
        }
    }

    private static def formatErrorMessage(List<Map<String, Object>> mismatches) {
        def table = new AsciiTable()
        table.addRule()
        table.addRow(null, "Version(s) mismatch has been detected").setTextAlignment(TextAlignment.CENTER)
        table.addRule()

        mismatches.each { Map<String, Object> current ->
            table.addRow(null, current.lib).setTextAlignment(TextAlignment.CENTER)
            table.addRule()
            current.versions.each { descriptor ->
                table.addRow(descriptor.plugin, descriptor.version)
                table.addRule()
            }
        }
        table.render()
    }

    @TaskAction
    def check() {
        project.logger.lifecycle("Checking UI Lib Versions on Deploy server")

        def plugins = Paths.get(ServerUtil.getServerWorkingDir(project))
                .resolve("plugins")
                .resolve("xld-official").toFile().listFiles()

        if (plugins != null) {
            def metadata = collectPluginMetadata(project, plugins.toList())
            def mismatches = findMismatches(metadata)
            if (!mismatches.isEmpty()) {
                throw new GradleException(formatErrorMessage(mismatches))
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
