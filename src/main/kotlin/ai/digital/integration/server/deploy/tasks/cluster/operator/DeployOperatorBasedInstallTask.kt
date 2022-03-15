package ai.digital.integration.server.deploy.tasks.cluster.operator

import ai.digital.integration.server.deploy.tasks.cli.DownloadXlCliDistTask
import ai.digital.integration.server.deploy.tasks.server.operator.StartDeployServerForOperatorInstanceTask
import org.gradle.api.DefaultTask

abstract class DeployOperatorBasedInstallTask : DefaultTask() {

    fun dependsOnTasks(): Array<String> {
        return arrayOf(
            StartDeployServerForOperatorInstanceTask.NAME,
            OperatorBasedStartDeployClusterTask.NAME,
            DownloadXlCliDistTask.NAME
        )
    }
}
