package ai.digital.integration.server.release

import ai.digital.integration.server.common.tasks.database.PrepareDatabaseTask
import ai.digital.integration.server.deploy.tasks.maintenance.CleanupBeforeStartupTask
import ai.digital.integration.server.deploy.tasks.server.ApplicationConfigurationOverrideTask
import ai.digital.integration.server.deploy.tasks.server.ServerCopyOverlaysTask
import ai.digital.integration.server.deploy.tasks.server.operator.OperatorCentralConfigurationTask
import ai.digital.integration.server.deploy.tasks.server.operator.PrepareOperatorServerTask
import ai.digital.integration.server.deploy.tasks.server.operator.StartDeployServerForOperatorInstanceTask
import ai.digital.integration.server.deploy.tasks.server.operator.StopDeployServerForOperatorInstanceTask
import ai.digital.integration.server.release.tasks.DockerBasedStopReleaseTask
import ai.digital.integration.server.release.tasks.StopReleaseIntegrationServerTask
import ai.digital.integration.server.release.tasks.cluster.StartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.StopReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.CheckingOutReleaseKubernetesOperatorTask
import ai.digital.integration.server.release.tasks.cluster.operator.OperatorBasedStartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.OperatorBasedStopReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.awseks.OperatorBasedAwsEksReleaseClusterStartTask
import ai.digital.integration.server.release.tasks.cluster.operator.awseks.OperatorBasedAwsEksReleaseClusterStopTask
import ai.digital.integration.server.release.tasks.cluster.operator.awsopenshift.OperatorBasedAwsOpenShiftReleaseClusterStartTask
import ai.digital.integration.server.release.tasks.cluster.operator.awsopenshift.OperatorBasedAwsOpenShiftReleaseClusterStopTask
import ai.digital.integration.server.release.tasks.cluster.operator.azureaks.OperatorBasedAzureAksStartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.azureaks.OperatorBasedAzureAksStopReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.gcpgke.OperatorBasedGcpGkeStartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.gcpgke.OperatorBasedGcpGkeStopReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.onprem.OperatorBasedOnPremStartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.onprem.OperatorBasedOnPremStopReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.vmwareopenshift.OperatorBasedVmWareOpenShiftStartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.vmwareopenshift.OperatorBasedVmWareOpenShiftStopReleaseClusterTask
import ai.digital.integration.server.release.tasks.server.StartReleaseServerInstanceTask
import ai.digital.integration.server.release.tasks.server.operator.StartReleaseToGetLicenceTask
import org.gradle.api.Project

open class ReleaseTaskRegistry {

    companion object {
        fun register(project: Project) {
            //Cluster

            project.tasks.create(StartReleaseClusterTask.NAME, StartReleaseClusterTask::class.java)
            project.tasks.create(StopReleaseClusterTask.NAME, StopReleaseClusterTask::class.java)

            project.tasks.create(StartReleaseIntegrationServerTask.NAME, StartReleaseIntegrationServerTask::class.java)
            project.tasks.create(StopReleaseIntegrationServerTask.NAME, StopReleaseIntegrationServerTask::class.java)

            project.tasks.create(StartReleaseServerInstanceTask.NAME, StartReleaseServerInstanceTask::class.java)
            project.tasks.create(DockerBasedStopReleaseTask.NAME, DockerBasedStopReleaseTask::class.java)

            // Cluster Operator
            project.tasks.create(OperatorBasedAwsEksReleaseClusterStartTask.NAME,
                OperatorBasedAwsEksReleaseClusterStartTask::class.java)
            project.tasks.create(OperatorBasedAwsEksReleaseClusterStopTask.NAME,
                OperatorBasedAwsEksReleaseClusterStopTask::class.java)

            project.tasks.create(OperatorBasedAwsOpenShiftReleaseClusterStartTask.NAME,
                OperatorBasedAwsOpenShiftReleaseClusterStartTask::class.java)
            project.tasks.create(OperatorBasedAwsOpenShiftReleaseClusterStopTask.NAME,
                OperatorBasedAwsOpenShiftReleaseClusterStopTask::class.java)

            project.tasks.create(OperatorBasedAzureAksStartReleaseClusterTask.NAME,
                OperatorBasedAzureAksStartReleaseClusterTask::class.java)
            project.tasks.create(OperatorBasedAzureAksStopReleaseClusterTask.NAME,
                OperatorBasedAzureAksStopReleaseClusterTask::class.java)

            project.tasks.create(OperatorBasedGcpGkeStartReleaseClusterTask.NAME,
                OperatorBasedGcpGkeStartReleaseClusterTask::class.java)
            project.tasks.create(OperatorBasedGcpGkeStopReleaseClusterTask.NAME,
                OperatorBasedGcpGkeStopReleaseClusterTask::class.java)

            project.tasks.create(OperatorBasedOnPremStartReleaseClusterTask.NAME,
                OperatorBasedOnPremStartReleaseClusterTask::class.java)
            project.tasks.create(OperatorBasedOnPremStopReleaseClusterTask.NAME,
                OperatorBasedOnPremStopReleaseClusterTask::class.java)

            project.tasks.create(OperatorBasedVmWareOpenShiftStartReleaseClusterTask.NAME,
                OperatorBasedVmWareOpenShiftStartReleaseClusterTask::class.java)
            project.tasks.create(OperatorBasedVmWareOpenShiftStopReleaseClusterTask.NAME,
                OperatorBasedVmWareOpenShiftStopReleaseClusterTask::class.java)

            project.tasks.create(OperatorBasedStartReleaseClusterTask.NAME,
                OperatorBasedStartReleaseClusterTask::class.java)
            project.tasks.create(OperatorBasedStopReleaseClusterTask.NAME,
                OperatorBasedStopReleaseClusterTask::class.java)

            project.tasks.create(CheckingOutReleaseKubernetesOperatorTask.NAME,
                CheckingOutReleaseKubernetesOperatorTask::class.java)

            project.tasks.create(StartReleaseToGetLicenceTask.NAME, StartReleaseToGetLicenceTask::class.java)

            // Operator Deploy Server Tasks

            project.tasks.create(StartDeployServerForOperatorInstanceTask.NAME,
                StartDeployServerForOperatorInstanceTask::class.java)
            project.tasks.create(StopDeployServerForOperatorInstanceTask.NAME,
                StopDeployServerForOperatorInstanceTask::class.java)

            project.tasks.create(ApplicationConfigurationOverrideTask.NAME,
                ApplicationConfigurationOverrideTask::class.java)
            project.tasks.create(CleanupBeforeStartupTask.NAME, CleanupBeforeStartupTask::class.java)
            project.tasks.create(OperatorCentralConfigurationTask.NAME, OperatorCentralConfigurationTask::class.java)
            project.tasks.create(PrepareDatabaseTask.NAME, PrepareDatabaseTask::class.java)
            project.tasks.create(PrepareOperatorServerTask.NAME, PrepareOperatorServerTask::class.java)
            project.tasks.create(ServerCopyOverlaysTask.NAME, ServerCopyOverlaysTask::class.java)
        }
    }
}
