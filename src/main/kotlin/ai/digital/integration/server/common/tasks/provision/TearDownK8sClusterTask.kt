package ai.digital.integration.server.common.tasks.provision

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.util.ProviderUtil
import ai.digital.integration.server.common.util.TerraformHelper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class TearDownK8sClusterTask: DefaultTask() {
    companion object {
        const val NAME = "tearDownK8sCluster"
    }

    init {
        this.group = PluginConstant.PLUGIN_GROUP
    }

    @TaskAction
    fun launch(){

        ProviderUtil.getProviders(project).forEach { provider ->
            project.logger.lifecycle("Tearing down kubernetes cluster on ${provider.name}")
        }
        TerraformHelper(project).teardownCluster()
    }

}