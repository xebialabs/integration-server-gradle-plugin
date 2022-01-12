package ai.digital.integration.server.common.tasks

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.tasks.ShutdownDeployIntegrationServerTask
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.release.tasks.StopReleaseIntegrationServerTask
import ai.digital.integration.server.release.util.ReleaseServerUtil
import org.gradle.api.DefaultTask

open class ShutdownIntegrationServerTask : DefaultTask() {

    companion object {
        const val NAME = "shutdownIntegrationServer"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        if (DeployServerUtil.isDeployServerDefined(project)) {
            this.dependsOn(ShutdownDeployIntegrationServerTask.NAME)
        }
        if (ReleaseServerUtil.isReleaseServerDefined(project)) {
            this.dependsOn(StopReleaseIntegrationServerTask.NAME)
        }
    }
}
