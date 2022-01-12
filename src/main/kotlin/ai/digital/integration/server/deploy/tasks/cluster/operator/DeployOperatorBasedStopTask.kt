package ai.digital.integration.server.deploy.tasks.cluster.operator

import ai.digital.integration.server.deploy.tasks.cli.DownloadAndExtractCliDistTask
import org.gradle.api.DefaultTask

abstract class DeployOperatorBasedStopTask : DefaultTask() {

    fun dependsOnTasks(): Array<String> {
        return arrayOf(
            DownloadAndExtractCliDistTask.NAME
        )
    }
}
