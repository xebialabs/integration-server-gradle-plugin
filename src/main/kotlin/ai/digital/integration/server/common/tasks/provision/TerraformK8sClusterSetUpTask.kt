package ai.digital.integration.server.common.tasks.provision

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.TerraformHelper
import ai.digital.integration.server.common.util.TerraformUtil.Companion.getProvider
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class TerraformK8sClusterSetUpTask : DefaultTask() {

    companion object {
        const val NAME = "terraformK8sClusterSetUp"
    }

    init {
        this.group = PLUGIN_GROUP
    }

    @TaskAction
    fun launch(){
        project.logger.lifecycle("Setting up a kubernetes cluster on ${getProvider(project)}")
        TerraformHelper(project).launchCluster()
    }

}
