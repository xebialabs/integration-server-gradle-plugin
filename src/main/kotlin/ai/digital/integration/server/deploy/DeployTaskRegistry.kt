package ai.digital.integration.server.deploy

import ai.digital.integration.server.common.gitlab.GitlabStartTask
import ai.digital.integration.server.common.gitlab.GitlabStopTask
import ai.digital.integration.server.common.mq.ShutdownMqTask
import ai.digital.integration.server.common.mq.StartMqTask
import ai.digital.integration.server.common.pluginManager.StartPluginManagerTask
import ai.digital.integration.server.common.tasks.database.DatabaseStartTask
import ai.digital.integration.server.common.tasks.database.DatabaseStopTask
import ai.digital.integration.server.common.tasks.database.ImportDbUnitDataTask
import ai.digital.integration.server.common.tasks.database.PrepareDatabaseTask
import ai.digital.integration.server.common.tasks.infrastructure.InfrastructureStartTask
import ai.digital.integration.server.common.tasks.infrastructure.InfrastructureStopTask
import ai.digital.integration.server.deploy.tasks.StartDeployIntegrationServerTask
import ai.digital.integration.server.deploy.tasks.ShutdownDeployIntegrationServerTask
import ai.digital.integration.server.deploy.tasks.anonymizer.ExportDatabaseTask
import ai.digital.integration.server.deploy.tasks.cli.*
import ai.digital.integration.server.deploy.tasks.cluster.StartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.StopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.dockercompose.DockerComposeBasedStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.dockercompose.DockerComposeBasedStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.HelmBasedInstallDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.HelmBasedStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.HelmBasedStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.ProvideDeployKubernetesHelmChartTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.awseks.HelmBasedAwsEksInstallDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.awseks.HelmBasedAwsEksStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.awseks.HelmBasedAwsEksStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.awsopenshift.HelmBasedAwsOpenShiftInstallDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.awsopenshift.HelmBasedAwsOpenShiftStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.awsopenshift.HelmBasedAwsOpenShiftStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.azureaks.HelmBasedAzureAksInstallDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.azureaks.HelmBasedAzureAksStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.azureaks.HelmBasedAzureAksStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.gcpgke.HelmBasedGcpGkeInstallDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.gcpgke.HelmBasedGcpGkeStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.gcpgke.HelmBasedGcpGkeStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.onprem.HelmBasedOnPremInstallDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.onprem.HelmBasedOnPremStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.onprem.HelmBasedOnPremStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.*
import ai.digital.integration.server.deploy.tasks.cluster.operator.awseks.OperatorBasedAwsEksInstallDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.awseks.OperatorBasedAwsEksStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.awseks.OperatorBasedAwsEksStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.awsopenshift.OperatorBasedAwsOpenShiftInstallDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.awsopenshift.OperatorBasedAwsOpenShiftStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.awsopenshift.OperatorBasedAwsOpenShiftStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.azureaks.OperatorBasedAzureAksInstallDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.azureaks.OperatorBasedAzureAksStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.azureaks.OperatorBasedAzureAksStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.gcpgke.OperatorBasedGcpGkeInstallDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.gcpgke.OperatorBasedGcpGkeStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.gcpgke.OperatorBasedGcpGkeStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.onprem.OperatorBasedOnPremInstallDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.onprem.OperatorBasedOnPremStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.onprem.OperatorBasedOnPremStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.vmwareopenshift.OperatorBasedVmWareOpenShiftInstallDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.vmwareopenshift.OperatorBasedVmWareOpenShiftStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.vmwareopenshift.OperatorBasedVmWareOpenShiftStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.terraform.TerraformBasedAwsEksStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.terraform.TerraformBasedAwsEksStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.maintenance.CleanupBeforeStartupTask
import ai.digital.integration.server.deploy.tasks.provision.RunDatasetGenerationTask
import ai.digital.integration.server.deploy.tasks.provision.RunDevOpsAsCodeTask
import ai.digital.integration.server.deploy.tasks.satellite.*
import ai.digital.integration.server.deploy.tasks.server.*
import ai.digital.integration.server.deploy.tasks.server.docker.DockerBasedStopDeployTask
import ai.digital.integration.server.deploy.tasks.server.operator.*
import ai.digital.integration.server.deploy.tasks.tests.IntegrationTestsTask
import ai.digital.integration.server.deploy.tasks.tls.GenerateSecureAkkaKeysTask
import ai.digital.integration.server.deploy.tasks.tls.TlsApplicationConfigurationOverrideTask
import ai.digital.integration.server.deploy.tasks.worker.*
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

open class DeployTaskRegistry {

    companion object {
        fun register(project: Project, itcfg: Configuration) {

            //CLI
            project.tasks.create(CliCleanDefaultExtTask.NAME, CliCleanDefaultExtTask::class.java)
            project.tasks.create(CopyCliBuildArtifactsTask.NAME, CopyCliBuildArtifactsTask::class.java)
            project.tasks.create(CliOverlaysTask.NAME, CliOverlaysTask::class.java)
            project.tasks.create(RunCliTask.NAME, RunCliTask::class.java)

            //Cluster

            project.tasks.create(StartDeployClusterTask.NAME, StartDeployClusterTask::class.java)
            project.tasks.create(StopDeployClusterTask.NAME, StopDeployClusterTask::class.java)

            // Cluster Operator
            project.tasks.create(OperatorBasedAwsEksStartDeployClusterTask.NAME,
                OperatorBasedAwsEksStartDeployClusterTask::class.java)
            project.tasks.create(OperatorBasedAwsEksInstallDeployClusterTask.NAME,
                OperatorBasedAwsEksInstallDeployClusterTask::class.java)
            project.tasks.create(OperatorBasedAwsEksStopDeployClusterTask.NAME,
                OperatorBasedAwsEksStopDeployClusterTask::class.java)

            project.tasks.create(OperatorBasedAwsOpenShiftStartDeployClusterTask.NAME,
                OperatorBasedAwsOpenShiftStartDeployClusterTask::class.java)
            project.tasks.create(OperatorBasedAwsOpenShiftInstallDeployClusterTask.NAME,
                OperatorBasedAwsOpenShiftInstallDeployClusterTask::class.java)
            project.tasks.create(OperatorBasedAwsOpenShiftStopDeployClusterTask.NAME,
                OperatorBasedAwsOpenShiftStopDeployClusterTask::class.java)

            project.tasks.create(OperatorBasedAzureAksStartDeployClusterTask.NAME,
                OperatorBasedAzureAksStartDeployClusterTask::class.java)
            project.tasks.create(OperatorBasedAzureAksInstallDeployClusterTask.NAME,
                OperatorBasedAzureAksInstallDeployClusterTask::class.java)
            project.tasks.create(OperatorBasedAzureAksStopDeployClusterTask.NAME,
                OperatorBasedAzureAksStopDeployClusterTask::class.java)

            project.tasks.create(OperatorBasedGcpGkeStartDeployClusterTask.NAME,
                OperatorBasedGcpGkeStartDeployClusterTask::class.java)
            project.tasks.create(OperatorBasedGcpGkeInstallDeployClusterTask.NAME,
                OperatorBasedGcpGkeInstallDeployClusterTask::class.java)
            project.tasks.create(OperatorBasedGcpGkeStopDeployClusterTask.NAME,
                OperatorBasedGcpGkeStopDeployClusterTask::class.java)

            project.tasks.create(OperatorBasedOnPremStartDeployClusterTask.NAME,
                OperatorBasedOnPremStartDeployClusterTask::class.java)
            project.tasks.create(OperatorBasedOnPremInstallDeployClusterTask.NAME,
                OperatorBasedOnPremInstallDeployClusterTask::class.java)
            project.tasks.create(OperatorBasedOnPremStopDeployClusterTask.NAME,
                OperatorBasedOnPremStopDeployClusterTask::class.java)

            project.tasks.create(OperatorBasedVmWareOpenShiftStartDeployClusterTask.NAME,
                OperatorBasedVmWareOpenShiftStartDeployClusterTask::class.java)
            project.tasks.create(OperatorBasedVmWareOpenShiftInstallDeployClusterTask.NAME,
                OperatorBasedVmWareOpenShiftInstallDeployClusterTask::class.java)
            project.tasks.create(OperatorBasedVmWareOpenShiftStopDeployClusterTask.NAME,
                OperatorBasedVmWareOpenShiftStopDeployClusterTask::class.java)

            project.tasks.create(OperatorBasedStartDeployClusterTask.NAME,
                OperatorBasedStartDeployClusterTask::class.java)
            project.tasks.create(OperatorBasedInstallDeployClusterTask.NAME,
                OperatorBasedInstallDeployClusterTask::class.java)
            project.tasks.create(OperatorBasedStopDeployClusterTask.NAME,
                OperatorBasedStopDeployClusterTask::class.java)

            project.tasks.create(ProvideDeployKubernetesOperatorTask.NAME,
                ProvideDeployKubernetesOperatorTask::class.java)
            project.tasks.create(OperatorCentralConfigurationTask.NAME, OperatorCentralConfigurationTask::class.java)
            project.tasks.create(StartDeployServerForOperatorInstanceTask.NAME,
                StartDeployServerForOperatorInstanceTask::class.java)
            project.tasks.create(StopDeployServerForOperatorInstanceTask.NAME,
                StopDeployServerForOperatorInstanceTask::class.java)
            project.tasks.create(PrepareOperatorServerTask.NAME,
                PrepareOperatorServerTask::class.java)
            project.tasks.create(OperatorBasedUpgradeDeployClusterTask.NAME, OperatorBasedUpgradeDeployClusterTask::class.java)

            // Cluster Helm

            project.tasks.create(HelmBasedStartDeployClusterTask.NAME,
                    HelmBasedStartDeployClusterTask::class.java)
            project.tasks.create(HelmBasedInstallDeployClusterTask.NAME,
                    HelmBasedInstallDeployClusterTask::class.java)
            project.tasks.create(HelmBasedStopDeployClusterTask.NAME,
                    HelmBasedStopDeployClusterTask::class.java)

            project.tasks.create(ProvideDeployKubernetesHelmChartTask.NAME,
                    ProvideDeployKubernetesHelmChartTask::class.java)

            project.tasks.create(HelmBasedAzureAksStartDeployClusterTask.NAME,
                    HelmBasedAzureAksStartDeployClusterTask::class.java)
            project.tasks.create(HelmBasedAzureAksInstallDeployClusterTask.NAME,
                    HelmBasedAzureAksInstallDeployClusterTask::class.java)
            project.tasks.create(HelmBasedAzureAksStopDeployClusterTask.NAME,
                    HelmBasedAzureAksStopDeployClusterTask::class.java)

            project.tasks.create(HelmBasedAwsEksStartDeployClusterTask.NAME,
                    HelmBasedAwsEksStartDeployClusterTask::class.java)
            project.tasks.create(HelmBasedAwsEksInstallDeployClusterTask.NAME,
                    HelmBasedAwsEksInstallDeployClusterTask::class.java)
            project.tasks.create(HelmBasedAwsEksStopDeployClusterTask.NAME,
                    HelmBasedAwsEksStopDeployClusterTask::class.java)

            project.tasks.create(HelmBasedGcpGkeStartDeployClusterTask.NAME,
                    HelmBasedGcpGkeStartDeployClusterTask::class.java)
            project.tasks.create(HelmBasedGcpGkeInstallDeployClusterTask.NAME,
                    HelmBasedGcpGkeInstallDeployClusterTask::class.java)
            project.tasks.create(HelmBasedGcpGkeStopDeployClusterTask.NAME,
                    HelmBasedGcpGkeStopDeployClusterTask::class.java)

            project.tasks.create(HelmBasedAwsOpenShiftStartDeployClusterTask.NAME,
                    HelmBasedAwsOpenShiftStartDeployClusterTask::class.java)
            project.tasks.create(HelmBasedAwsOpenShiftInstallDeployClusterTask.NAME,
                    HelmBasedAwsOpenShiftInstallDeployClusterTask::class.java)
            project.tasks.create(HelmBasedAwsOpenShiftStopDeployClusterTask.NAME,
                    HelmBasedAwsOpenShiftStopDeployClusterTask::class.java)

            project.tasks.create(HelmBasedOnPremStartDeployClusterTask.NAME,
                    HelmBasedOnPremStartDeployClusterTask::class.java)
            project.tasks.create(HelmBasedOnPremInstallDeployClusterTask.NAME,
                    HelmBasedOnPremInstallDeployClusterTask::class.java)
            project.tasks.create(HelmBasedOnPremStopDeployClusterTask.NAME,
                    HelmBasedOnPremStopDeployClusterTask::class.java)


            // Cluster Terraform
            project.tasks.create(TerraformBasedAwsEksStartDeployClusterTask.NAME,
                TerraformBasedAwsEksStartDeployClusterTask::class.java)
            project.tasks.create(TerraformBasedAwsEksStopDeployClusterTask.NAME,
                TerraformBasedAwsEksStopDeployClusterTask::class.java)

            // Cluster Docker Compose
            project.tasks.create(DockerComposeBasedStartDeployClusterTask.NAME,
                DockerComposeBasedStartDeployClusterTask::class.java)
            project.tasks.create(DockerComposeBasedStopDeployClusterTask.NAME,
                DockerComposeBasedStopDeployClusterTask::class.java)

            //Database
            project.tasks.create(DatabaseStartTask.NAME, DatabaseStartTask::class.java)
            project.tasks.create(DatabaseStopTask.NAME, DatabaseStopTask::class.java)

            //Deploy Server
            project.tasks.create(ApplicationConfigurationOverrideTask.NAME,
                ApplicationConfigurationOverrideTask::class.java)
            project.tasks.create(CentralConfigurationTask.NAME, CentralConfigurationTask::class.java)
            project.tasks.create(CheckUILibVersionsTask.NAME, CheckUILibVersionsTask::class.java)
            project.tasks.create(CopyServerBuildArtifactsTask.NAME, CopyServerBuildArtifactsTask::class.java)
            project.tasks.create(CopyServerFoldersTask.NAME, CopyServerFoldersTask::class.java)
            project.tasks.create(ServerCopyOverlaysTask.NAME, ServerCopyOverlaysTask::class.java)
            project.tasks.create(DockerBasedStopDeployTask.NAME, DockerBasedStopDeployTask::class.java)
            project.tasks.create(DownloadAndExtractDbUnitDataDistTask.NAME,
                DownloadAndExtractDbUnitDataDistTask::class.java)
            project.tasks.create(DownloadAndExtractServerDistTask.NAME, DownloadAndExtractServerDistTask::class.java)
            project.tasks.create(ExportDatabaseTask.NAME, ExportDatabaseTask::class.java)
            project.tasks.create(GenerateSecureAkkaKeysTask.NAME, GenerateSecureAkkaKeysTask::class.java)
            project.tasks.create(ImportDbUnitDataTask.NAME, ImportDbUnitDataTask::class.java)
            project.tasks.create(PrepareDatabaseTask.NAME, PrepareDatabaseTask::class.java)
            project.tasks.create(PrepareServerTask.NAME, PrepareServerTask::class.java)
            project.tasks.create(RunDatasetGenerationTask.NAME, RunDatasetGenerationTask::class.java)
            project.tasks.create(RunDevOpsAsCodeTask.NAME, RunDevOpsAsCodeTask::class.java)
            project.tasks.create(SetServerLogbackLevelsTask.NAME, SetServerLogbackLevelsTask::class.java)
            project.tasks.create(ServerYamlPatchTask.NAME, ServerYamlPatchTask::class.java)
            project.tasks.create(StartDeployServerInstanceTask.NAME, StartDeployServerInstanceTask::class.java)
            project.tasks.create(TlsApplicationConfigurationOverrideTask.NAME,
                TlsApplicationConfigurationOverrideTask::class.java)

            //Infrastructure
            project.tasks.create(GitlabStartTask.NAME, GitlabStartTask::class.java)
            project.tasks.create(GitlabStopTask.NAME, GitlabStopTask::class.java)
            project.tasks.create(InfrastructureStopTask.NAME, InfrastructureStopTask::class.java)
            project.tasks.create(InfrastructureStartTask.NAME, InfrastructureStartTask::class.java)

            //Integration Server
            project.tasks.create(ShutdownDeployIntegrationServerTask.NAME,
                ShutdownDeployIntegrationServerTask::class.java)
            project.tasks.create(StartDeployIntegrationServerTask.NAME, StartDeployIntegrationServerTask::class.java)
                .dependsOn(itcfg)

            //Maintenance
            project.tasks.create(CleanupBeforeStartupTask.NAME, CleanupBeforeStartupTask::class.java)

            //MQ
            project.tasks.create(ShutdownMqTask.NAME, ShutdownMqTask::class.java)
            project.tasks.create(StartMqTask.NAME, StartMqTask::class.java)

            //Plugin Manager
            project.tasks.create(StartPluginManagerTask.NAME, StartPluginManagerTask::class.java)

            //Satellite
            project.tasks.create(DownloadAndExtractSatelliteDistTask.NAME,
                DownloadAndExtractSatelliteDistTask::class.java)
            project.tasks.create(PrepareSatellitesTask.NAME, PrepareSatellitesTask::class.java)
            project.tasks.create(SatelliteOverlaysTask.NAME, SatelliteOverlaysTask::class.java)
            project.tasks.create(SatelliteSyncPluginsTask.NAME, SatelliteSyncPluginsTask::class.java)
            project.tasks.create(ShutdownSatelliteTask.NAME, ShutdownSatelliteTask::class.java)
            project.tasks.create(StartSatelliteTask.NAME, StartSatelliteTask::class.java)

            //Workers
            project.tasks.create(CopyIntegrationServerTask.NAME, CopyIntegrationServerTask::class.java)
            project.tasks.create(SyncServerPluginsWithWorkerTask.NAME, SyncServerPluginsWithWorkerTask::class.java)
            project.tasks.create(DownloadAndExtractWorkerDistTask.NAME, DownloadAndExtractWorkerDistTask::class.java)
            project.tasks.create(SetWorkersLogbackLevelsTask.NAME, SetWorkersLogbackLevelsTask::class.java)
            project.tasks.create(ShutdownWorkersTask.NAME, ShutdownWorkersTask::class.java)
            project.tasks.create(StartWorkersTask.NAME, StartWorkersTask::class.java)
            project.tasks.create(WorkerOverlaysTask.NAME, WorkerOverlaysTask::class.java)
            project.tasks.create(PrepareWorkersTask.NAME, PrepareWorkersTask::class.java)

            //Tests
            project.tasks.create(IntegrationTestsTask.NAME, IntegrationTestsTask::class.java)
        }
    }
}
