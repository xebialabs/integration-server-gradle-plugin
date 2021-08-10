package ai.digital.integration.server.tasks

import ai.digital.integration.server.util.LocationUtil
import ai.digital.integration.server.util.ServerUtil
import ai.digital.integration.server.util.YamlFileUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

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
