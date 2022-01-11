package ai.digital.integration.server.release

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.release.tasks.cluster.StartReleaseClusterTask
import ai.digital.integration.server.release.tasks.server.StartReleaseServerInstanceTask
import ai.digital.integration.server.release.util.ReleaseServerUtil
import org.gradle.api.DefaultTask

open class StartReleaseIntegrationServerTask : DefaultTask() {

    companion object {
        const val NAME = "startReleaseIntegrationServer"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP
        if (ReleaseServerUtil.isClusterEnabled(project)) {
            this.dependsOn(StartReleaseClusterTask.NAME)
        } else {
            this.dependsOn(StartReleaseServerInstanceTask.NAME)
        }
    }
}
