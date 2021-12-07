package ai.digital.integration.server.common

import ai.digital.integration.server.common.tasks.ShutdownIntegrationServerTask
import ai.digital.integration.server.common.tasks.StartIntegrationServerTask
import org.gradle.api.Project

class TaskRegistry {

    companion object {
        fun register(project: Project) {
            project.tasks.create(StartIntegrationServerTask.NAME, StartIntegrationServerTask::class.java)
            project.tasks.create(ShutdownIntegrationServerTask.NAME, ShutdownIntegrationServerTask::class.java)
        }
    }
}
