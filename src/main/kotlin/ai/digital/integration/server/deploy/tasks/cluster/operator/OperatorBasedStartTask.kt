package ai.digital.integration.server.deploy.tasks.cluster.operator

import ai.digital.integration.server.deploy.tasks.cli.DownloadAndExtractCliDistTask
import ai.digital.integration.server.deploy.tasks.server.StartDeployServerForOperatorInstanceTask
import org.gradle.api.DefaultTask

abstract class OperatorBasedStartTask : DefaultTask() {

    fun dependsOnTasks(): Array<String> {
        return arrayOf(
            DownloadAndExtractCliDistTask.NAME,
            StartDeployServerForOperatorInstanceTask.NAME,
            CheckingOutDeployKubernetesOperatorTask.NAME
        )
    }
}
