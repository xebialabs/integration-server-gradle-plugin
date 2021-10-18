package ai.digital.integration.server.common.tasks

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.tasks.StartDeployIntegrationServerTask
import ai.digital.integration.server.deploy.util.DeployServerUtil
import ai.digital.integration.server.release.StartReleaseIntegrationServerTask
import ai.digital.integration.server.release.util.ReleaseServerUtil
import org.gradle.api.DefaultTask

open class StartIntegrationServerTask : DefaultTask() {

    companion object {
        const val NAME = "startIntegrationServer"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        if (DeployServerUtil.isDeployServerDefined(project)) {
            this.dependsOn(StartDeployIntegrationServerTask.NAME)
        }
        if (ReleaseServerUtil.isReleaseServerDefined(project)) {
            this.dependsOn(StartReleaseIntegrationServerTask.NAME)
        }
    }
}
