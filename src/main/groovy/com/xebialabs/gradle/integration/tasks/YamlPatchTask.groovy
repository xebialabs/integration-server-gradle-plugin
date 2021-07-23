package com.xebialabs.gradle.integration.tasks


import com.xebialabs.gradle.integration.util.ExtensionsUtil
import com.xebialabs.gradle.integration.util.YamlFileUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.util.PluginUtil.PLUGIN_GROUP

class YamlPatchTask extends DefaultTask {
    static NAME = "yamlPatches"

    YamlPatchTask() {
        def dependencies = [
                CentralConfigurationTask.NAME
        ]

        this.configure { ->
            group = PLUGIN_GROUP
            dependsOn(dependencies)
        }
    }

    @TaskAction
    def yamlPatches() {
        project.logger.lifecycle("Applying yaml patches on central configuration files")
        def yamlPatches = ExtensionsUtil.getExtension(project).yamlPatches

        if (yamlPatches && yamlPatches.size() > 0) {
            yamlPatches.each { yamlPatch ->
                def file = new File("${ExtensionsUtil.getServerWorkingDir(project)}/${yamlPatch.key}")
                YamlFileUtil.overlayFile(file, yamlPatch.value)
            }
        }
    }
}
