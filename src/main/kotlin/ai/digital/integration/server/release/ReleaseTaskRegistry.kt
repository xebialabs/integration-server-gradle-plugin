package ai.digital.integration.server.release

import org.gradle.api.Project

open class ReleaseTaskRegistry {

    companion object {
        fun register(project: Project) {
            project.tasks.create(StartReleaseIntegrationServerTask.NAME, StartReleaseIntegrationServerTask::class.java)
        }
    }
}
