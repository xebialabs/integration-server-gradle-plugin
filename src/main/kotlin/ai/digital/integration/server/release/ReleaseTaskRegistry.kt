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
import ai.digital.integration.server.release.tasks.cluster.helm.HelmBasedInstallReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.HelmBasedStartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.HelmBasedStopReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.ProvideReleaseKubernetesHelmChartTask
import ai.digital.integration.server.release.tasks.cluster.helm.awseks.HelmBasedAwsEksInstallReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.awseks.HelmBasedAwsEksStartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.awseks.HelmBasedAwsEksStopReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.awsopenshift.HelmBasedAwsOpenShiftInstallReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.awsopenshift.HelmBasedAwsOpenShiftStartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.awsopenshift.HelmBasedAwsOpenShiftStopReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.azureaks.HelmBasedAzureAksInstallReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.azureaks.HelmBasedAzureAksStartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.azureaks.HelmBasedAzureAksStopReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.gcpgke.HelmBasedGcpGkeInstallReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.gcpgke.HelmBasedGcpGkeStartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.gcpgke.HelmBasedGcpGkeStopReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.onprem.HelmBasedOnPremInstallReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.onprem.HelmBasedOnPremStartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.onprem.HelmBasedOnPremStopReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.*
import ai.digital.integration.server.release.tasks.cluster.operator.awseks.OperatorBasedAwsEksInstallReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.awseks.OperatorBasedAwsEksStartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.awseks.OperatorBasedAwsEksStopReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.awsopenshift.OperatorBasedAwsOpenShiftInstallReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.awsopenshift.OperatorBasedAwsOpenShiftStartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.awsopenshift.OperatorBasedAwsOpenShiftStopReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.azureaks.OperatorBasedAzureAksInstallReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.azureaks.OperatorBasedAzureAksStartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.azureaks.OperatorBasedAzureAksStopReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.gcpgke.OperatorBasedGcpGkeInstallReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.gcpgke.OperatorBasedGcpGkeStartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.gcpgke.OperatorBasedGcpGkeStopReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.onprem.OperatorBasedOnPremInstallReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.onprem.OperatorBasedOnPremStartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.onprem.OperatorBasedOnPremStopReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.vmwareopenshift.OperatorBasedVmWareOpenShiftInstallReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.vmwareopenshift.OperatorBasedVmWareOpenShiftStartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.vmwareopenshift.OperatorBasedVmWareOpenShiftStopReleaseClusterTask
import ai.digital.integration.server.release.tasks.server.StartReleaseServerInstanceTask
import ai.digital.integration.server.release.tasks.server.operator.StartReleaseToGetLicenceTask
import org.gradle.api.Project

open class ReleaseTaskRegistry {

    companion object {
        fun register(project: Project) {

            //Cluster

            project.tasks.register(StartReleaseClusterTask.NAME, StartReleaseClusterTask::class.java)
            project.tasks.register(StopReleaseClusterTask.NAME, StopReleaseClusterTask::class.java)

            project.tasks.register(StartReleaseIntegrationServerTask.NAME, StartReleaseIntegrationServerTask::class.java)
            project.tasks.register(StopReleaseIntegrationServerTask.NAME, StopReleaseIntegrationServerTask::class.java)

            project.tasks.register(StartReleaseServerInstanceTask.NAME, StartReleaseServerInstanceTask::class.java)
            project.tasks.register(DockerBasedStopReleaseTask.NAME, DockerBasedStopReleaseTask::class.java)

            // Cluster Helm
            project.tasks.register(HelmBasedAwsEksStartReleaseClusterTask.NAME,
                    HelmBasedAwsEksStartReleaseClusterTask::class.java)
            project.tasks.register(HelmBasedAwsEksInstallReleaseClusterTask.NAME,
                    HelmBasedAwsEksInstallReleaseClusterTask::class.java)
            project.tasks.register(HelmBasedAwsEksStopReleaseClusterTask.NAME,
                    HelmBasedAwsEksStopReleaseClusterTask::class.java)

            project.tasks.register(HelmBasedAwsOpenShiftStartReleaseClusterTask.NAME,
                    HelmBasedAwsOpenShiftStartReleaseClusterTask::class.java)
            project.tasks.register(HelmBasedAwsOpenShiftInstallReleaseClusterTask.NAME,
                    HelmBasedAwsOpenShiftInstallReleaseClusterTask::class.java)
            project.tasks.register(HelmBasedAwsOpenShiftStopReleaseClusterTask.NAME,
                    HelmBasedAwsOpenShiftStopReleaseClusterTask::class.java)

            project.tasks.register(HelmBasedAzureAksStartReleaseClusterTask.NAME,
                    HelmBasedAzureAksStartReleaseClusterTask::class.java)
            project.tasks.register(HelmBasedAzureAksInstallReleaseClusterTask.NAME,
                    HelmBasedAzureAksInstallReleaseClusterTask::class.java)
            project.tasks.register(HelmBasedAzureAksStopReleaseClusterTask.NAME,
                    HelmBasedAzureAksStopReleaseClusterTask::class.java)

            project.tasks.register(HelmBasedGcpGkeStartReleaseClusterTask.NAME,
                    HelmBasedGcpGkeStartReleaseClusterTask::class.java)
            project.tasks.register(HelmBasedGcpGkeInstallReleaseClusterTask.NAME,
                    HelmBasedGcpGkeInstallReleaseClusterTask::class.java)
            project.tasks.register(HelmBasedGcpGkeStopReleaseClusterTask.NAME,
                    HelmBasedGcpGkeStopReleaseClusterTask::class.java)

            project.tasks.register(HelmBasedOnPremStartReleaseClusterTask.NAME,
                    HelmBasedOnPremStartReleaseClusterTask::class.java)
            project.tasks.register(HelmBasedOnPremInstallReleaseClusterTask.NAME,
                    HelmBasedOnPremInstallReleaseClusterTask::class.java)
            project.tasks.register(HelmBasedOnPremStopReleaseClusterTask.NAME,
                    HelmBasedOnPremStopReleaseClusterTask::class.java)

            project.tasks.register(HelmBasedStartReleaseClusterTask.NAME,
                    HelmBasedStartReleaseClusterTask::class.java)
            project.tasks.register(HelmBasedInstallReleaseClusterTask.NAME,
                    HelmBasedInstallReleaseClusterTask::class.java)
            project.tasks.register(HelmBasedStopReleaseClusterTask.NAME,
                    HelmBasedStopReleaseClusterTask::class.java)

            project.tasks.register(ProvideReleaseKubernetesHelmChartTask.NAME,
                    ProvideReleaseKubernetesHelmChartTask::class.java)


            // Cluster Operator
            project.tasks.register(OperatorBasedAwsEksStartReleaseClusterTask.NAME,
                OperatorBasedAwsEksStartReleaseClusterTask::class.java)
            project.tasks.register(OperatorBasedAwsEksInstallReleaseClusterTask.NAME,
                OperatorBasedAwsEksInstallReleaseClusterTask::class.java)
            project.tasks.register(OperatorBasedAwsEksStopReleaseClusterTask.NAME,
                OperatorBasedAwsEksStopReleaseClusterTask::class.java)

            project.tasks.register(OperatorBasedAwsOpenShiftStartReleaseClusterTask.NAME,
                OperatorBasedAwsOpenShiftStartReleaseClusterTask::class.java)
            project.tasks.register(OperatorBasedAwsOpenShiftInstallReleaseClusterTask.NAME,
                OperatorBasedAwsOpenShiftInstallReleaseClusterTask::class.java)
            project.tasks.register(OperatorBasedAwsOpenShiftStopReleaseClusterTask.NAME,
                OperatorBasedAwsOpenShiftStopReleaseClusterTask::class.java)

            project.tasks.register(OperatorBasedAzureAksStartReleaseClusterTask.NAME,
                OperatorBasedAzureAksStartReleaseClusterTask::class.java)
            project.tasks.register(OperatorBasedAzureAksInstallReleaseClusterTask.NAME,
                OperatorBasedAzureAksInstallReleaseClusterTask::class.java)
            project.tasks.register(OperatorBasedAzureAksStopReleaseClusterTask.NAME,
                OperatorBasedAzureAksStopReleaseClusterTask::class.java)

            project.tasks.register(OperatorBasedGcpGkeStartReleaseClusterTask.NAME,
                OperatorBasedGcpGkeStartReleaseClusterTask::class.java)
            project.tasks.register(OperatorBasedGcpGkeInstallReleaseClusterTask.NAME,
                OperatorBasedGcpGkeInstallReleaseClusterTask::class.java)
            project.tasks.register(OperatorBasedGcpGkeStopReleaseClusterTask.NAME,
                OperatorBasedGcpGkeStopReleaseClusterTask::class.java)

            project.tasks.register(OperatorBasedOnPremStartReleaseClusterTask.NAME,
                OperatorBasedOnPremStartReleaseClusterTask::class.java)
            project.tasks.register(OperatorBasedOnPremInstallReleaseClusterTask.NAME,
                OperatorBasedOnPremInstallReleaseClusterTask::class.java)
            project.tasks.register(OperatorBasedOnPremStopReleaseClusterTask.NAME,
                OperatorBasedOnPremStopReleaseClusterTask::class.java)

            project.tasks.register(OperatorBasedVmWareOpenShiftStartReleaseClusterTask.NAME,
                OperatorBasedVmWareOpenShiftStartReleaseClusterTask::class.java)
            project.tasks.register(OperatorBasedVmWareOpenShiftInstallReleaseClusterTask.NAME,
                OperatorBasedVmWareOpenShiftInstallReleaseClusterTask::class.java)
            project.tasks.register(OperatorBasedVmWareOpenShiftStopReleaseClusterTask.NAME,
                OperatorBasedVmWareOpenShiftStopReleaseClusterTask::class.java)

            project.tasks.register(OperatorBasedStartReleaseClusterTask.NAME,
                OperatorBasedStartReleaseClusterTask::class.java)
            project.tasks.register(OperatorBasedInstallReleaseClusterTask.NAME,
                OperatorBasedInstallReleaseClusterTask::class.java)
            project.tasks.register(OperatorBasedStopReleaseClusterTask.NAME,
                OperatorBasedStopReleaseClusterTask::class.java)

            project.tasks.register(ProvideReleaseKubernetesOperatorTask.NAME,
                ProvideReleaseKubernetesOperatorTask::class.java)

            project.tasks.register(OperatorBasedUpgradeReleaseClusterTask.NAME,
                    OperatorBasedUpgradeReleaseClusterTask::class.java)

            project.tasks.register(StartReleaseToGetLicenceTask.NAME, StartReleaseToGetLicenceTask::class.java)

            // Operator Deploy Server Tasks

            project.tasks.register(StartDeployServerForOperatorInstanceTask.NAME,
                StartDeployServerForOperatorInstanceTask::class.java)
            project.tasks.register(StopDeployServerForOperatorInstanceTask.NAME,
                StopDeployServerForOperatorInstanceTask::class.java)

            project.tasks.register(ApplicationConfigurationOverrideTask.NAME,
                ApplicationConfigurationOverrideTask::class.java)
            project.tasks.register(CleanupBeforeStartupTask.NAME, CleanupBeforeStartupTask::class.java)
            project.tasks.register(OperatorCentralConfigurationTask.NAME, OperatorCentralConfigurationTask::class.java)
            project.tasks.register(PrepareDatabaseTask.NAME, PrepareDatabaseTask::class.java)
            project.tasks.register(PrepareOperatorServerTask.NAME, PrepareOperatorServerTask::class.java)
            project.tasks.register(ServerCopyOverlaysTask.NAME, ServerCopyOverlaysTask::class.java)
        }
    }
}
