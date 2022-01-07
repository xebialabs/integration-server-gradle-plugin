package ai.digital.integration.server.release

import ai.digital.integration.server.release.tasks.cluster.StartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.StopReleaseClusterTask
import org.gradle.api.Project

open class ReleaseTaskRegistry {

    companion object {
        fun register(project: Project) {
            //Cluster

            project.tasks.create(StartReleaseClusterTask.NAME, StartReleaseClusterTask::class.java)
            project.tasks.create(StopReleaseClusterTask.NAME, StopReleaseClusterTask::class.java)

            project.tasks.create(StartReleaseIntegrationServerTask.NAME, StartReleaseIntegrationServerTask::class.java)
        }
    }
}
