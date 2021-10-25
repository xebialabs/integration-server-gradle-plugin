package ai.digital.integration.server.deploy.tasks.server

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.common.util.YamlFileUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class ServerYamlPatchTask : DefaultTask() {

    companion object {
        const val NAME = "serverYamlPatch"
    }

    init {
        this.group = PLUGIN_GROUP
        this.dependsOn(CentralConfigurationTask.NAME)
        this.dependsOn(ServerCopyOverlaysTask.NAME)
    }

    @TaskAction
    fun yamlPatches() {
        DeployServerUtil.getServers(project)
                .forEach { server ->
                    project.logger.lifecycle("Applying patches on YAML files for ${server.name}.")

                    server.yamlPatches.forEach { yamlPatch ->
                        val file = File("${DeployServerUtil.getServerWorkingDir(project, server)}/${yamlPatch.key}")
                        YamlFileUtil.overlayFile(file, yamlPatch.value.toMutableMap())
                    }
                }
    }
}
