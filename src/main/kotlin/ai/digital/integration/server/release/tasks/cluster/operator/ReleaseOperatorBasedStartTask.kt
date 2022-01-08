package ai.digital.integration.server.release.tasks.cluster.operator

import ai.digital.integration.server.deploy.tasks.cli.DownloadAndExtractCliDistTask
import ai.digital.integration.server.release.tasks.server.operator.StartReleaseServerForOperatorInstanceTask
import org.gradle.api.DefaultTask

abstract class ReleaseOperatorBasedStartTask : DefaultTask() {

    fun dependsOnTasks(): Array<String> {
        return arrayOf(
            DownloadAndExtractCliDistTask.NAME,
            StartReleaseServerForOperatorInstanceTask.NAME,
            CheckingOutReleaseKubernetesOperatorTask.NAME
        )
    }
}
