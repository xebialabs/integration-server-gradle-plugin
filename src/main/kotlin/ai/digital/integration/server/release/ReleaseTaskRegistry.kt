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

            project.tasks.create(StartReleaseClusterTask.NAME, StartReleaseClusterTask::class.java)
            project.tasks.create(StopReleaseClusterTask.NAME, StopReleaseClusterTask::class.java)

            project.tasks.create(StartReleaseIntegrationServerTask.NAME, StartReleaseIntegrationServerTask::class.java)
            project.tasks.create(StopReleaseIntegrationServerTask.NAME, StopReleaseIntegrationServerTask::class.java)

            project.tasks.create(StartReleaseServerInstanceTask.NAME, StartReleaseServerInstanceTask::class.java)
            project.tasks.create(DockerBasedStopReleaseTask.NAME, DockerBasedStopReleaseTask::class.java)

            // Cluster Helm
            project.tasks.create(HelmBasedAwsEksStartReleaseClusterTask.NAME,
                    HelmBasedAwsEksStartReleaseClusterTask::class.java)
            project.tasks.create(HelmBasedAwsEksInstallReleaseClusterTask.NAME,
                    HelmBasedAwsEksInstallReleaseClusterTask::class.java)
            project.tasks.create(HelmBasedAwsEksStopReleaseClusterTask.NAME,
                    HelmBasedAwsEksStopReleaseClusterTask::class.java)

            project.tasks.create(HelmBasedAwsOpenShiftStartReleaseClusterTask.NAME,
                    HelmBasedAwsOpenShiftStartReleaseClusterTask::class.java)
            project.tasks.create(HelmBasedAwsOpenShiftInstallReleaseClusterTask.NAME,
                    HelmBasedAwsOpenShiftInstallReleaseClusterTask::class.java)
            project.tasks.create(HelmBasedAwsOpenShiftStopReleaseClusterTask.NAME,
                    HelmBasedAwsOpenShiftStopReleaseClusterTask::class.java)

            project.tasks.create(HelmBasedOnPremStartReleaseClusterTask.NAME,
                    HelmBasedOnPremStartReleaseClusterTask::class.java)
            project.tasks.create(HelmBasedOnPremInstallReleaseClusterTask.NAME,
                    HelmBasedOnPremInstallReleaseClusterTask::class.java)
            project.tasks.create(HelmBasedOnPremStopReleaseClusterTask.NAME,
                    HelmBasedOnPremStopReleaseClusterTask::class.java)

            project.tasks.create(HelmBasedStartReleaseClusterTask.NAME,
                    HelmBasedStartReleaseClusterTask::class.java)
            project.tasks.create(HelmBasedInstallReleaseClusterTask.NAME,
                    HelmBasedInstallReleaseClusterTask::class.java)
            project.tasks.create(HelmBasedStopReleaseClusterTask.NAME,
                    HelmBasedStopReleaseClusterTask::class.java)

            project.tasks.create(ProvideReleaseKubernetesHelmChartTask.NAME,
                    ProvideReleaseKubernetesHelmChartTask::class.java)


            // Cluster Operator
            project.tasks.create(OperatorBasedAwsEksStartReleaseClusterTask.NAME,
                OperatorBasedAwsEksStartReleaseClusterTask::class.java)
            project.tasks.create(OperatorBasedAwsEksInstallReleaseClusterTask.NAME,
                OperatorBasedAwsEksInstallReleaseClusterTask::class.java)
            project.tasks.create(OperatorBasedAwsEksStopReleaseClusterTask.NAME,
                OperatorBasedAwsEksStopReleaseClusterTask::class.java)

            project.tasks.create(OperatorBasedAwsOpenShiftStartReleaseClusterTask.NAME,
                OperatorBasedAwsOpenShiftStartReleaseClusterTask::class.java)
            project.tasks.create(OperatorBasedAwsOpenShiftInstallReleaseClusterTask.NAME,
                OperatorBasedAwsOpenShiftInstallReleaseClusterTask::class.java)
            project.tasks.create(OperatorBasedAwsOpenShiftStopReleaseClusterTask.NAME,
                OperatorBasedAwsOpenShiftStopReleaseClusterTask::class.java)

            project.tasks.create(OperatorBasedAzureAksStartReleaseClusterTask.NAME,
                OperatorBasedAzureAksStartReleaseClusterTask::class.java)
            project.tasks.create(OperatorBasedAzureAksInstallReleaseClusterTask.NAME,
                OperatorBasedAzureAksInstallReleaseClusterTask::class.java)
            project.tasks.create(OperatorBasedAzureAksStopReleaseClusterTask.NAME,
                OperatorBasedAzureAksStopReleaseClusterTask::class.java)

            project.tasks.create(OperatorBasedGcpGkeStartReleaseClusterTask.NAME,
                OperatorBasedGcpGkeStartReleaseClusterTask::class.java)
            project.tasks.create(OperatorBasedGcpGkeInstallReleaseClusterTask.NAME,
                OperatorBasedGcpGkeInstallReleaseClusterTask::class.java)
            project.tasks.create(OperatorBasedGcpGkeStopReleaseClusterTask.NAME,
                OperatorBasedGcpGkeStopReleaseClusterTask::class.java)

            project.tasks.create(OperatorBasedOnPremStartReleaseClusterTask.NAME,
                OperatorBasedOnPremStartReleaseClusterTask::class.java)
            project.tasks.create(OperatorBasedOnPremInstallReleaseClusterTask.NAME,
                OperatorBasedOnPremInstallReleaseClusterTask::class.java)
            project.tasks.create(OperatorBasedOnPremStopReleaseClusterTask.NAME,
                OperatorBasedOnPremStopReleaseClusterTask::class.java)

            project.tasks.create(OperatorBasedVmWareOpenShiftStartReleaseClusterTask.NAME,
                OperatorBasedVmWareOpenShiftStartReleaseClusterTask::class.java)
            project.tasks.create(OperatorBasedVmWareOpenShiftInstallReleaseClusterTask.NAME,
                OperatorBasedVmWareOpenShiftInstallReleaseClusterTask::class.java)
            project.tasks.create(OperatorBasedVmWareOpenShiftStopReleaseClusterTask.NAME,
                OperatorBasedVmWareOpenShiftStopReleaseClusterTask::class.java)

            project.tasks.create(OperatorBasedStartReleaseClusterTask.NAME,
                OperatorBasedStartReleaseClusterTask::class.java)
            project.tasks.create(OperatorBasedInstallReleaseClusterTask.NAME,
                OperatorBasedInstallReleaseClusterTask::class.java)
            project.tasks.create(OperatorBasedStopReleaseClusterTask.NAME,
                OperatorBasedStopReleaseClusterTask::class.java)

            project.tasks.create(ProvideReleaseKubernetesOperatorTask.NAME,
                ProvideReleaseKubernetesOperatorTask::class.java)

            project.tasks.create(OperatorBasedUpgradeReleaseClusterTask.NAME,
                    OperatorBasedUpgradeReleaseClusterTask::class.java)

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
