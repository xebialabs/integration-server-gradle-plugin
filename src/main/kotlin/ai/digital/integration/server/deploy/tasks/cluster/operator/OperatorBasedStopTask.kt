package ai.digital.integration.server.deploy.tasks.cluster.operator

import ai.digital.integration.server.deploy.tasks.cli.DownloadAndExtractCliDistTask
import ai.digital.integration.server.deploy.tasks.server.operator.StartDeployServerForOperatorInstanceTask
import org.gradle.api.DefaultTask

abstract class OperatorBasedStopTask : DefaultTask() {

    fun dependsOnTasks(): Array<String> {
        return arrayOf(
            DownloadAndExtractCliDistTask.NAME,
            StartDeployServerForOperatorInstanceTask.NAME
        )
    }
}
