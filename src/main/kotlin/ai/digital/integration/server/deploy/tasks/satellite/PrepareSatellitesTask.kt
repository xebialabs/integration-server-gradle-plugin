package ai.digital.integration.server.deploy.tasks.satellite

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.deploy.tasks.tls.GenerateSecureAkkaKeysTask
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.internals.SatelliteInitializeUtil
import ai.digital.integration.server.deploy.internals.SatelliteUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class PrepareSatellitesTask : DefaultTask() {

    init {
        this.group = PLUGIN_GROUP

        this.dependsOn(SatelliteOverlaysTask.NAME)
        if (DeployServerUtil.isAkkaSecured(project)) {
            this.dependsOn(GenerateSecureAkkaKeysTask.NAME)
        }
    }

    @TaskAction
    fun launch() {
        SatelliteUtil.getSatellites(project).forEach { satellite ->
            SatelliteInitializeUtil.prepare(project, satellite)
        }
    }

    companion object {
        const val NAME = "prepareSatellites"
    }
}
