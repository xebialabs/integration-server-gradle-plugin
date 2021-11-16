package ai.digital.integration.server.common.tasks.provision

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.ProviderUtil.Companion.getProviders
import ai.digital.integration.server.common.util.ProviderUtil.Companion.getProvidersNames
import ai.digital.integration.server.common.util.TerraformHelper
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
        project.logger.lifecycle("Setting up a kubernetes cluster on ${getProvidersNames(getProviders(project))}")
        TerraformHelper(project).launchCluster()
    }

}
