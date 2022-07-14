package ai.digital.integration.server.deploy.tasks.centralConfiguration

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.CentralConfigurationServerUtil
import ai.digital.integration.server.common.util.YamlFileUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class CentralConfigurationServerYamlPatchTask : DefaultTask() {
    companion object {
        const val NAME = "centralConfigurationServerYamlPatch"
    }

    init {
        this.group = PluginConstant.PLUGIN_GROUP
        this.onlyIf {
            CentralConfigurationServerUtil.hasCentralConfigurationServer(project)
        }

        this.mustRunAfter(PrepareCentralConfigurationServerTask.NAME)
        this.mustRunAfter(CentralConfigurationServerOverlaysTask.NAME)
    }

    @TaskAction
    fun yamlPatches() {
        val server = CentralConfigurationServerUtil.getCentralConfigurationServer(project)
        project.logger.lifecycle("Applying patches on YAML files for ${server.name}.")

        server.yamlPatches.forEach { yamlPatch ->
            val file = File("${CentralConfigurationServerUtil.getServerPath(project, server)}/${yamlPatch.key}")
            YamlFileUtil.overlayFileWithJackson(file, yamlPatch.value.toMutableMap())
        }
    }
}