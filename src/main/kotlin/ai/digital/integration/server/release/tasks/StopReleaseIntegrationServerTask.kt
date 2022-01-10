package ai.digital.integration.server.release.tasks

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.tasks.database.DatabaseStopTask
import ai.digital.integration.server.common.util.DbUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.internals.ShutdownUtil
import ai.digital.integration.server.deploy.tasks.cluster.StopDeployClusterTask
import ai.digital.integration.server.release.tasks.cluster.StopReleaseClusterTask
import ai.digital.integration.server.release.util.ReleaseServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class StopReleaseIntegrationServerTask : DefaultTask() {

    companion object {
        const val NAME = "stopReleaseIntegrationServer"
    }

    init {
        this.group = PLUGIN_GROUP

        val that = this
        project.afterEvaluate {
            if (ReleaseServerUtil.isClusterEnabled(project)) {
                that.dependsOn(StopReleaseClusterTask.NAME)
            } else {
                if (ReleaseServerUtil.isDockerBased(project)) {
                    that.dependsOn(DockerBasedStopReleaseTask.NAME)
                }

                if (DbUtil.isDerby(project)) {
                    that.finalizedBy("derbyStop")
                } else {
                    that.finalizedBy(DatabaseStopTask.NAME)
                }
            }
        }
    }

    @TaskAction
    fun shutdown() {
        project.logger.lifecycle("About to shutting down Release Server.")

        if (!ReleaseServerUtil.isDockerBased(project)) {
            ShutdownUtil.shutdownServer(project)
        }
    }
}
