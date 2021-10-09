package ai.digital.integration.server.tasks

import ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.tasks.database.DatabaseStopTask
import ai.digital.integration.server.tasks.satellite.ShutdownSatelliteTask
import ai.digital.integration.server.tasks.worker.ShutdownWorkersTask
import ai.digital.integration.server.util.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class ShutdownIntegrationServerTask : DefaultTask() {

    companion object {
        @JvmStatic
        val NAME = "shutdownIntegrationServer"
    }

    init {
        this.group = PLUGIN_GROUP

        if (DeployServerUtil.isDockerBased(project)) {
            this.dependsOn(DockerBasedStopDeployTask.NAME)
        }
        if (WorkerUtil.hasWorkers(project)) {
            this.dependsOn(ShutdownWorkersTask.NAME)
        }
        if (SatelliteUtil.hasSatellites(project)) {
            this.dependsOn(ShutdownSatelliteTask.NAME)
        }
        if (DbUtil.isDerby(project)) {
            this.finalizedBy("derbyStop")
        } else {
            this.finalizedBy(DatabaseStopTask.NAME)
        }
    }

    @TaskAction
    fun shutdown() {
        project.logger.lifecycle("About to shutting down Deploy Server.")

        if (!DeployServerUtil.isDockerBased(project)) {
            ShutdownUtil.shutdownServer(project)
        }
    }
}
