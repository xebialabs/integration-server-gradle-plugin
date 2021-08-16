package ai.digital.integration.server.tasks

import ai.digital.integration.server.tasks.database.DatabaseStopTask
import ai.digital.integration.server.tasks.satellite.ShutdownSatelliteTask
import ai.digital.integration.server.tasks.worker.ShutdownWorkersTask
import ai.digital.integration.server.util.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class ShutdownIntegrationServerTask extends DefaultTask {
    static NAME = "shutdownIntegrationServer"

    ShutdownIntegrationServerTask() {
        def dependencies = []
        if (ServerUtil.isDockerBased(project)) {
            dependencies.push(DockerBasedStopDeployTask.NAME)
        }
        if (WorkerUtil.hasWorkers(project)) {
            dependencies.push(ShutdownWorkersTask.NAME)
        }
        if (SatelliteUtil.hasSatellites(project)) {
            dependencies.push(ShutdownSatelliteTask.NAME)
        }
        group = PLUGIN_GROUP
        if (DbUtil.isDerby(project)) {
            finalizedBy("derbyStop")
        } else {
            finalizedBy(DatabaseStopTask.NAME)
        }
        this.configure {
            group = PLUGIN_GROUP
            if (!dependencies.empty) {
                dependsOn(dependencies)
            }
        }
    }

    @TaskAction
    void shutdown() {
        project.logger.lifecycle("About to shutting down Deploy Server.")

        if (!ServerUtil.isDockerBased(project)) {
            ShutdownUtil.shutdownServer(project)
        }
    }
}
