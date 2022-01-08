package ai.digital.integration.server.release.tasks.cluster.operator

import org.gradle.api.DefaultTask

abstract class ReleaseOperatorBasedStopTask : DefaultTask() {

    fun dependsOnTasks(): Array<String> {
        return arrayOf()
    }
}
