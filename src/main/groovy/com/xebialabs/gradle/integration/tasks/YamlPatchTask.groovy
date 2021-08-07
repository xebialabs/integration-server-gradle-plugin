package com.xebialabs.gradle.integration.tasks


import com.xebialabs.gradle.integration.util.LocationUtil
import com.xebialabs.gradle.integration.util.ServerUtil
import com.xebialabs.gradle.integration.util.YamlFileUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.xebialabs.gradle.integration.constant.PluginConstant.PLUGIN_GROUP

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
        def server = ServerUtil.getServer(project)
        project.logger.lifecycle("Applying patches on YAML files for ${server.name}.")

        server.yamlPatches.each { Map.Entry<String, Map<String, Object>> yamlPatch ->
            def file = new File("${LocationUtil.getServerWorkingDir(project)}/${yamlPatch.key}")
            YamlFileUtil.overlayFile(file, yamlPatch.value)
        }
    }
}
