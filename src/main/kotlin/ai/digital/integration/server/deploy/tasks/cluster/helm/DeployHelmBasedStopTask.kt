package ai.digital.integration.server.deploy.tasks.cluster.helm

import ai.digital.integration.server.deploy.tasks.cli.DownloadAndExtractCliDistTask
import org.gradle.api.DefaultTask

abstract class DeployHelmBasedStopTask : DefaultTask() {

    fun dependsOnTasks(): Array<String> {
        return arrayOf(
            DownloadAndExtractCliDistTask.NAME
        )
    }
}
