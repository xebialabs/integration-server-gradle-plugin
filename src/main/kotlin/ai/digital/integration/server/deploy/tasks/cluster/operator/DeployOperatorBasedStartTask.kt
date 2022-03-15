package ai.digital.integration.server.deploy.tasks.cluster.operator

import ai.digital.integration.server.deploy.tasks.cli.DownloadAndExtractCliDistTask
import ai.digital.integration.server.deploy.tasks.maintenance.CleanupBeforeStartupTask
import ai.digital.integration.server.deploy.tasks.server.ServerCopyOverlaysTask
import ai.digital.integration.server.deploy.tasks.server.operator.PrepareOperatorServerTask
import org.gradle.api.DefaultTask

abstract class DeployOperatorBasedStartTask : DefaultTask() {

    fun dependsOnTasks(): Array<String> {
        return arrayOf(
            CleanupBeforeStartupTask.NAME,
            DownloadAndExtractCliDistTask.NAME,
            ProvideDeployKubernetesOperatorTask.NAME,
            PrepareOperatorServerTask.NAME,
            ServerCopyOverlaysTask.NAME
        )
    }
}
