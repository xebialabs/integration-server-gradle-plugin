package ai.digital.integration.server.release.tasks

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.DbUtil
import ai.digital.integration.server.common.tasks.database.DatabaseStopTask
import ai.digital.integration.server.deploy.internals.ShutdownUtil
import ai.digital.integration.server.release.util.ReleaseServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class ShutdownReleaseIntegrationServerTask : DefaultTask() {

    companion object {
        const val NAME = "shutdownReleaseIntegrationServer"
    }

    init {
        this.group = PLUGIN_GROUP

        if (ReleaseServerUtil.isDockerBased(project)) {
            this.dependsOn(DockerBasedStopReleaseTask.NAME)
        }

        if (DbUtil.isDerby(project)) {
            this.finalizedBy("derbyStop")
        } else {
            this.finalizedBy(DatabaseStopTask.NAME)
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
