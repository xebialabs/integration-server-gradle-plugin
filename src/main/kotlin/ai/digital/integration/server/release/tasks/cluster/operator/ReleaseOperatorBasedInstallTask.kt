package ai.digital.integration.server.release.tasks.cluster.operator

import ai.digital.integration.server.deploy.tasks.cli.DownloadXlCliDistTask
import ai.digital.integration.server.deploy.tasks.server.operator.StartDeployServerForOperatorInstanceTask
import org.gradle.api.DefaultTask

abstract class ReleaseOperatorBasedInstallTask : DefaultTask() {

    fun dependsOnTasks(): Array<String> {
        return arrayOf(
            StartDeployServerForOperatorInstanceTask.NAME,
            OperatorBasedStartReleaseClusterTask.NAME,
            DownloadXlCliDistTask.NAME
        )
    }
}
