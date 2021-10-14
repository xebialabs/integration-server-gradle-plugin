package ai.digital.integration.server.tasks.satellite

import ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.tasks.GenerateSecureAkkaKeysTask
import ai.digital.integration.server.util.DeployServerUtil
import ai.digital.integration.server.util.SatelliteInitializeUtil
import ai.digital.integration.server.util.SatelliteUtil
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
