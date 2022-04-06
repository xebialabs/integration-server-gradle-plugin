package ai.digital.integration.server.deploy.tasks.cluster.helm

import ai.digital.integration.server.deploy.tasks.cli.DownloadAndExtractCliDistTask
import ai.digital.integration.server.deploy.tasks.maintenance.CleanupBeforeStartupTask
import org.gradle.api.DefaultTask

abstract class DeployHelmBasedStartTask : DefaultTask() {

    fun dependsOnTasks(): Array<String> {
        return arrayOf(
            CleanupBeforeStartupTask.NAME,
            DownloadAndExtractCliDistTask.NAME,
            ProvideDeployKubernetesHelmChartTask.NAME
                    )
    }
}
