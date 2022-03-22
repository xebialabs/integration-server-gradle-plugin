package ai.digital.integration.server.deploy.tasks

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.tasks.database.DatabaseStopTask
import ai.digital.integration.server.common.tasks.infrastructure.InfrastructureStopTask
import ai.digital.integration.server.common.util.DbUtil
import ai.digital.integration.server.common.util.InfrastructureUtil
import ai.digital.integration.server.deploy.internals.*
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import ai.digital.integration.server.deploy.tasks.cluster.StopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.satellite.ShutdownSatelliteTask
import ai.digital.integration.server.deploy.tasks.server.docker.DockerBasedStopCCTask
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

        val that = this
        project.afterEvaluate {
            if (DeployServerUtil.isClusterEnabled(project)) {
                that.dependsOn(StopDeployClusterTask.NAME)
            } else {
                if (DeployServerUtil.isDockerBased(project)) {
                    that.dependsOn(DockerBasedStopDeployTask.NAME)
                }
                if (CentralConfigurationStandaloneUtil.isDockerBased(project)) {
                    that.dependsOn(DockerBasedStopCCTask.NAME)
                }
                if (WorkerUtil.hasWorkers(project)) {
                    that.dependsOn(ShutdownWorkersTask.NAME)
                }
                if (SatelliteUtil.hasSatellites(project)) {
                    that.dependsOn(ShutdownSatelliteTask.NAME)
                }
                if (DbUtil.isDerby(project)) {
                    that.finalizedBy("derbyStop")
                } else {
                    that.finalizedBy(DatabaseStopTask.NAME)
                }
                if (InfrastructureUtil.hasInfrastructures(project)){
                    that.finalizedBy(InfrastructureStopTask.NAME)
                }
            }
        }
    }

    @TaskAction
    fun shutdown() {
        project.logger.lifecycle("About to shutting down Deploy Server.")

        if (!DeployServerUtil.isDockerBased(project) && !DeployClusterUtil.isOperatorProvider(project)) {
            DeployShutdownUtil.shutdownServer(project)
        }
    }
}
