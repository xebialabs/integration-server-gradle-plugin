package ai.digital.integration.server.release.tasks.cluster.helm

import ai.digital.integration.server.deploy.tasks.cli.DownloadAndExtractCliDistTask
import ai.digital.integration.server.deploy.tasks.maintenance.CleanupBeforeStartupTask
import ai.digital.integration.server.release.tasks.server.operator.StartReleaseToGetLicenceTask
import org.gradle.api.DefaultTask

abstract class ReleaseHelmBasedStartTask : DefaultTask() {

    fun dependsOnTasks(): Array<String> {
        return arrayOf(
            CleanupBeforeStartupTask.NAME,
            DownloadAndExtractCliDistTask.NAME,
            ProvideReleaseKubernetesHelmChartTask.NAME,
            StartReleaseToGetLicenceTask.NAME
        )
    }
}
