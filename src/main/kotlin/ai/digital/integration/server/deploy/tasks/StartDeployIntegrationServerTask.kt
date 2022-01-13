package ai.digital.integration.server.deploy.tasks

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.tasks.cluster.StartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.server.StartDeployServerInstanceTask
import org.gradle.api.DefaultTask

open class StartDeployIntegrationServerTask : DefaultTask() {

    companion object {
        const val NAME = "startDeployIntegrationServer"
    }

    init {
        group = PLUGIN_GROUP
        if (DeployServerUtil.isClusterEnabled(project)) {
            this.dependsOn(StartDeployClusterTask.NAME)
        } else {
            this.dependsOn(StartDeployServerInstanceTask.NAME)
        }
    }
}
