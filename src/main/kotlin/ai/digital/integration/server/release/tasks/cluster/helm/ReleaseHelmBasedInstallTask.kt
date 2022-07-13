package ai.digital.integration.server.release.tasks.cluster.helm

import org.gradle.api.DefaultTask

abstract class ReleaseHelmBasedInstallTask : DefaultTask() {

    fun dependsOnTasks(): Array<String> {
        return arrayOf(
            HelmBasedStartReleaseClusterTask.NAME
        )
    }
}
