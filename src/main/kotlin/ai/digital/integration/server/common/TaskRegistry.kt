package ai.digital.integration.server.common

import ai.digital.integration.server.common.centralConfiguration.*
import ai.digital.integration.server.common.tasks.ShutdownIntegrationServerTask
import ai.digital.integration.server.common.tasks.StartIntegrationServerTask
import ai.digital.integration.server.deploy.tasks.cli.DownloadAndExtractCliDistTask
import ai.digital.integration.server.deploy.tasks.cli.DownloadXlCliDistTask
import ai.digital.integration.server.deploy.tasks.server.operator.StopDeployServerForOperatorUpgradeTask
import org.gradle.api.Project

class TaskRegistry {

    companion object {
        fun register(project: Project) {
            project.tasks.create(DownloadAndExtractCliDistTask.NAME, DownloadAndExtractCliDistTask::class.java)
            project.tasks.create(StartIntegrationServerTask.NAME, StartIntegrationServerTask::class.java)
            project.tasks.create(ShutdownIntegrationServerTask.NAME, ShutdownIntegrationServerTask::class.java)
            project.tasks.create(StopDeployServerForOperatorUpgradeTask.NAME,
                    StopDeployServerForOperatorUpgradeTask::class.java)
            project.tasks.create(DownloadXlCliDistTask.NAME, DownloadXlCliDistTask::class.java)

            //Central configuration service
            project.tasks.create(DownloadAndExtractCentralConfigurationServerDistTask.NAME, DownloadAndExtractCentralConfigurationServerDistTask::class.java)
            project.tasks.create(PrepareCentralConfigurationServerTask.NAME, PrepareCentralConfigurationServerTask::class.java)
            project.tasks.create(StartCentralConfigurationServerTask.NAME, StartCentralConfigurationServerTask::class.java)
            project.tasks.create(ShutdownCentralConfigurationServerTask.NAME, ShutdownCentralConfigurationServerTask::class.java)
        }
    }
}
