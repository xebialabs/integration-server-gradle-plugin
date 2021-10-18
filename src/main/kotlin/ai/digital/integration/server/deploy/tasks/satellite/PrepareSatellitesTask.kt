package ai.digital.integration.server.deploy.tasks.satellite

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.deploy.tasks.GenerateSecureAkkaKeysTask
import ai.digital.integration.server.deploy.util.DeployServerUtil
import ai.digital.integration.server.deploy.util.SatelliteInitializeUtil
import ai.digital.integration.server.deploy.util.SatelliteUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class PrepareSatellitesTask : DefaultTask() {

    init {
        this.dependsOn(SatelliteOverlaysTask.NAME)
        if (DeployServerUtil.isAkkaSecured(project)) {
            this.dependsOn(GenerateSecureAkkaKeysTask.NAME)
        }

        this.group = PLUGIN_GROUP
    }

    @TaskAction
    fun launch() {
        SatelliteUtil.getSatellites(project).forEach { satellite ->
            SatelliteInitializeUtil.prepare(project, satellite)
        }
    }

    companion object {
        @JvmStatic
        val NAME = "prepareSatellites"
    }
}
