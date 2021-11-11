package ai.digital.integration.server.common.tasks.provision

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.TerraformUtil
import ai.digital.integration.server.common.util.TerraformUtil.Companion.getProvider
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class SetUpK8sClusterTask : DefaultTask() {

    companion object {
        const val NAME = "setUpK8sCluster"
    }

    init {
        this.group = PLUGIN_GROUP
    }

    @TaskAction
    fun launch(){
        project.logger.lifecycle("Setting up a kubernetes cluster on ${getProvider(project)}")
        TerraformUtil.execute(project, listOf("init"))
        TerraformUtil.execute(project, listOf("apply"))
    }

}
