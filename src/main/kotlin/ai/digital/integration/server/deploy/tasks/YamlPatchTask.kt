package ai.digital.integration.server.deploy.tasks

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.deploy.util.DeployServerUtil
import ai.digital.integration.server.common.util.YamlFileUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class YamlPatchTask : DefaultTask() {

    companion object {
        const val NAME = "yamlPatch"
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
