package com.xebialabs.gradle.integration.tasks

import com.xebialabs.gradle.integration.util.ExtensionsUtil
import de.vandermeer.asciitable.AsciiTable
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment
import groovy.json.JsonSlurper
import org.apache.commons.io.IOUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import java.nio.charset.Charset
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class CheckUILibVersionsTask extends DefaultTask {
    static NAME = "checkUILibVersions"

    CheckUILibVersionsTask() {
        this.configure {
            group = PLUGIN_GROUP
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

    private static def collectPluginMetadata(List<File> files) {
        files.findAll { it.name.endsWith(".xldp") }.collectMany { plugin ->
            def xldpZip = new ZipFile(plugin)
            def internalJarEntry = xldpZip.entries().toList().find { it.name.endsWith(".jar") }
            internalJarEntry ? extractPluginMetadata(xldpZip, internalJarEntry) : []
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

        mismatches.each { current ->
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
        def plugins = Paths.get(ExtensionsUtil.getServerWorkingDir(project)).resolve("plugins").toFile().listFiles().toList()
        def metadata = collectPluginMetadata(plugins)
        def mismatches = findMismatches(metadata)
        if (!mismatches.isEmpty()) {
            throw new GradleException(formatErrorMessage(mismatches))
        }
    }
}
