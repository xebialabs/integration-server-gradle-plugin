package com.xebialabs.gradle.integration.tasks

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.xebialabs.gradle.integration.util.ExtensionsUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class YamlPatchesTask extends DefaultTask {
    static NAME = "yamlPatches"
    static def mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))

    YamlPatchesTask() {
        this.configure { ->
            group = PLUGIN_GROUP
            mustRunAfter DownloadAndExtractServerDistTask.NAME
        }
    }

    @TaskAction
    def yamlPatches() {
        project.logger.lifecycle("applying yaml patches")
        def yamlPatches = ExtensionsUtil.getExtension(project).yamlPatches
        if (yamlPatches && yamlPatches.size() > 0) {
            yamlPatches.each { yamlPatch ->
                def file = new File("${ExtensionsUtil.getServerWorkingDir(project)}/${yamlPatch.key}")
                if (!file.exists()) {
                    file.createNewFile()
                    mapper.writeValue(file, yamlPatch.value)
                } else {
                    def root = mapper.readTree(file)
                    yamlPatch.value.each { property ->
                        root.put(property.key, property.value)
                    }
                    mapper.writeValue(file, root)
                }
            }
        }
    }
}
