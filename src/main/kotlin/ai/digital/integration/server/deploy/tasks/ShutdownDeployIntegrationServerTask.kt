package ai.digital.integration.server.deploy.tasks

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.tasks.database.DatabaseStopTask
import ai.digital.integration.server.common.util.DbUtil
import ai.digital.integration.server.deploy.internals.*
import ai.digital.integration.server.deploy.internals.cluster.DeployDockerClusterHelper
import ai.digital.integration.server.deploy.tasks.cluster.StopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.satellite.ShutdownSatelliteTask
import ai.digital.integration.server.deploy.tasks.server.docker.DockerBasedStopDeployTask
import ai.digital.integration.server.deploy.tasks.worker.ShutdownWorkersTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class ShutdownDeployIntegrationServerTask : DefaultTask() {

    companion object {
        const val NAME = "shutdownDeployIntegrationServer"
    }

    init {
        this.group = PLUGIN_GROUP

        if (DeployDockerClusterHelper(project).isClusterEnabled()) {
            this.dependsOn(StopDeployClusterTask.NAME)
        } else {
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
    }

    @TaskAction
    fun shutdown() {
        project.logger.lifecycle("About to shutting down Deploy Server.")

        if (!DeployServerUtil.isDockerBased(project)) {
            ShutdownUtil.shutdownServer(project)
        }
    }
}
