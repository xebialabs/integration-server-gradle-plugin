package ai.digital.integration.server.tasks.satellite

import ai.digital.integration.server.domain.Satellite
import ai.digital.integration.server.util.SatelliteInitializeUtil
import ai.digital.integration.server.util.SatelliteUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class PrepareSatellitesTask extends DefaultTask {
    static NAME = "prepareSatellites"

    PrepareSatellitesTask() {
        def dependencies = [
                SatelliteOverlaysTask.NAME
        ]
        this.configure {
            group = PLUGIN_GROUP
            dependsOn(dependencies)
        }
    }

    @TaskAction
    void launch() {
        SatelliteUtil.getSatellites(project).forEach {
            Satellite satellite ->
                SatelliteInitializeUtil.prepare(project, satellite)
        }
    }
}