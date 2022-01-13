package ai.digital.integration.server.deploy.tasks.centralConfigServer

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.CentralConfigServerUtil
import ai.digital.integration.server.deploy.tasks.server.ServerYamlPatchTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class PrepareCCTask : DefaultTask() {

    init {
        this.group = PluginConstant.PLUGIN_GROUP
        this.dependsOn(ServerYamlPatchTask.NAME)
        this.dependsOn(CentralConfigOverlaysTask.NAME)
    }

    @TaskAction
    fun launch() {
        CentralConfigServerUtil.prepare(project)
    }

    companion object {
        const val NAME = "prepareCentralConfigServer"
    }
}
