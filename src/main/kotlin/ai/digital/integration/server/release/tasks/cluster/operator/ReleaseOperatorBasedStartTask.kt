package ai.digital.integration.server.release.tasks.cluster.operator

import ai.digital.integration.server.deploy.tasks.cli.DownloadAndExtractCliDistTask
import ai.digital.integration.server.deploy.tasks.maintenance.CleanupBeforeStartupTask
import ai.digital.integration.server.release.tasks.server.operator.StartReleaseToGetLicenceTask
import org.gradle.api.DefaultTask

abstract class ReleaseOperatorBasedStartTask : DefaultTask() {

    fun dependsOnTasks(): Array<String> {
        return arrayOf(
            CleanupBeforeStartupTask.NAME,
            DownloadAndExtractCliDistTask.NAME,
            ProvideReleaseKubernetesOperatorTask.NAME,
            StartReleaseToGetLicenceTask.NAME
        )
    }
}
