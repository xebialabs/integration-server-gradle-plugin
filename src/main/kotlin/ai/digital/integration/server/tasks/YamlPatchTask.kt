package ai.digital.integration.server.tasks

import ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.util.DeployServerUtil
import ai.digital.integration.server.util.YamlFileUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class YamlPatchTask : DefaultTask() {

    companion object {
        @JvmStatic
        val NAME = "yamlPatch"
    }

    init {
        this.group = PLUGIN_GROUP
        this.dependsOn(CentralConfigurationTask.NAME)
        this.dependsOn(CopyOverlaysTask.NAME)
    }

    @TaskAction
    fun yamlPatches() {
        val server = DeployServerUtil.getServer(project)
        project.logger.lifecycle("Applying patches on YAML files for ${server.name}.")

        server.yamlPatches.forEach { yamlPatch ->
            val file = File("${DeployServerUtil.getServerWorkingDir(project)}/${yamlPatch.key}")
            YamlFileUtil.overlayFile(file, yamlPatch.value.toMutableMap())
        }
    }
}
