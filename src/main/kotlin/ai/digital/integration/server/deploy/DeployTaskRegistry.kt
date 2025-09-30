package ai.digital.integration.server.deploy

import ai.digital.integration.server.common.cache.ShutdownCacheTask
import ai.digital.integration.server.common.cache.StartCacheTask
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
import ai.digital.integration.server.deploy.tasks.ShutdownDeployIntegrationServerTask
import ai.digital.integration.server.deploy.tasks.StartDeployIntegrationServerTask
import ai.digital.integration.server.deploy.tasks.anonymizer.ExportDatabaseTask
import ai.digital.integration.server.deploy.tasks.centralConfiguration.*
import ai.digital.integration.server.deploy.tasks.cli.CliCleanDefaultExtTask
import ai.digital.integration.server.deploy.tasks.cli.CliOverlaysTask
import ai.digital.integration.server.deploy.tasks.cli.CopyCliBuildArtifactsTask
import ai.digital.integration.server.deploy.tasks.cli.RunCliTask
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
import ai.digital.integration.server.deploy.tasks.server.operator.OperatorCentralConfigurationTask
import ai.digital.integration.server.deploy.tasks.server.operator.PrepareOperatorServerTask
import ai.digital.integration.server.deploy.tasks.server.operator.StartDeployServerForOperatorInstanceTask
import ai.digital.integration.server.deploy.tasks.server.operator.StopDeployServerForOperatorInstanceTask
import ai.digital.integration.server.deploy.tasks.tests.IntegrationTestsTask
import ai.digital.integration.server.deploy.tasks.tls.GenerateSecurePekkoKeysTask
import ai.digital.integration.server.deploy.tasks.tls.TlsApplicationConfigurationOverrideTask
import ai.digital.integration.server.deploy.tasks.worker.*
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

open class DeployTaskRegistry {

    companion object {
        fun register(project: Project, itcfg: Configuration) {

            //Cache
            project.tasks.register(StartCacheTask.NAME, StartCacheTask::class.java)
            project.tasks.register(ShutdownCacheTask.NAME, ShutdownCacheTask::class.java)

            //CLI
            project.tasks.register(CliCleanDefaultExtTask.NAME, CliCleanDefaultExtTask::class.java)
            project.tasks.register(CopyCliBuildArtifactsTask.NAME, CopyCliBuildArtifactsTask::class.java)
            project.tasks.register(CliOverlaysTask.NAME, CliOverlaysTask::class.java)
            project.tasks.register(RunCliTask.NAME, RunCliTask::class.java)

            //Cluster

            project.tasks.register(StartDeployClusterTask.NAME, StartDeployClusterTask::class.java)
            project.tasks.register(StopDeployClusterTask.NAME, StopDeployClusterTask::class.java)

            // Cluster Operator
            project.tasks.register(OperatorBasedAwsEksStartDeployClusterTask.NAME,
                OperatorBasedAwsEksStartDeployClusterTask::class.java)
            project.tasks.register(OperatorBasedAwsEksInstallDeployClusterTask.NAME,
                OperatorBasedAwsEksInstallDeployClusterTask::class.java)
            project.tasks.register(OperatorBasedAwsEksStopDeployClusterTask.NAME,
                OperatorBasedAwsEksStopDeployClusterTask::class.java)

            project.tasks.register(OperatorBasedAwsOpenShiftStartDeployClusterTask.NAME,
                OperatorBasedAwsOpenShiftStartDeployClusterTask::class.java)
            project.tasks.register(OperatorBasedAwsOpenShiftInstallDeployClusterTask.NAME,
                OperatorBasedAwsOpenShiftInstallDeployClusterTask::class.java)
            project.tasks.register(OperatorBasedAwsOpenShiftStopDeployClusterTask.NAME,
                OperatorBasedAwsOpenShiftStopDeployClusterTask::class.java)

            project.tasks.register(OperatorBasedAzureAksStartDeployClusterTask.NAME,
                OperatorBasedAzureAksStartDeployClusterTask::class.java)
            project.tasks.register(OperatorBasedAzureAksInstallDeployClusterTask.NAME,
                OperatorBasedAzureAksInstallDeployClusterTask::class.java)
            project.tasks.register(OperatorBasedAzureAksStopDeployClusterTask.NAME,
                OperatorBasedAzureAksStopDeployClusterTask::class.java)

            project.tasks.register(OperatorBasedGcpGkeStartDeployClusterTask.NAME,
                OperatorBasedGcpGkeStartDeployClusterTask::class.java)
            project.tasks.register(OperatorBasedGcpGkeInstallDeployClusterTask.NAME,
                OperatorBasedGcpGkeInstallDeployClusterTask::class.java)
            project.tasks.register(OperatorBasedGcpGkeStopDeployClusterTask.NAME,
                OperatorBasedGcpGkeStopDeployClusterTask::class.java)

            project.tasks.register(OperatorBasedOnPremStartDeployClusterTask.NAME,
                OperatorBasedOnPremStartDeployClusterTask::class.java)
            project.tasks.register(OperatorBasedOnPremInstallDeployClusterTask.NAME,
                OperatorBasedOnPremInstallDeployClusterTask::class.java)
            project.tasks.register(OperatorBasedOnPremStopDeployClusterTask.NAME,
                OperatorBasedOnPremStopDeployClusterTask::class.java)

            project.tasks.register(OperatorBasedVmWareOpenShiftStartDeployClusterTask.NAME,
                OperatorBasedVmWareOpenShiftStartDeployClusterTask::class.java)
            project.tasks.register(OperatorBasedVmWareOpenShiftInstallDeployClusterTask.NAME,
                OperatorBasedVmWareOpenShiftInstallDeployClusterTask::class.java)
            project.tasks.register(OperatorBasedVmWareOpenShiftStopDeployClusterTask.NAME,
                OperatorBasedVmWareOpenShiftStopDeployClusterTask::class.java)

            project.tasks.register(OperatorBasedStartDeployClusterTask.NAME,
                OperatorBasedStartDeployClusterTask::class.java)
            project.tasks.register(OperatorBasedInstallDeployClusterTask.NAME,
                OperatorBasedInstallDeployClusterTask::class.java)
            project.tasks.register(OperatorBasedStopDeployClusterTask.NAME,
                OperatorBasedStopDeployClusterTask::class.java)

            project.tasks.register(ProvideDeployKubernetesOperatorTask.NAME,
                ProvideDeployKubernetesOperatorTask::class.java)
            project.tasks.register(OperatorCentralConfigurationTask.NAME, OperatorCentralConfigurationTask::class.java)
            project.tasks.register(StartDeployServerForOperatorInstanceTask.NAME,
                StartDeployServerForOperatorInstanceTask::class.java)
            project.tasks.register(StopDeployServerForOperatorInstanceTask.NAME,
                StopDeployServerForOperatorInstanceTask::class.java)
            project.tasks.register(PrepareOperatorServerTask.NAME,
                PrepareOperatorServerTask::class.java)
            project.tasks.register(OperatorBasedUpgradeDeployClusterTask.NAME, OperatorBasedUpgradeDeployClusterTask::class.java)

            // Cluster Helm

            project.tasks.register(HelmBasedStartDeployClusterTask.NAME,
                    HelmBasedStartDeployClusterTask::class.java)
            project.tasks.register(HelmBasedInstallDeployClusterTask.NAME,
                    HelmBasedInstallDeployClusterTask::class.java)
            project.tasks.register(HelmBasedStopDeployClusterTask.NAME,
                    HelmBasedStopDeployClusterTask::class.java)

            project.tasks.register(ProvideDeployKubernetesHelmChartTask.NAME,
                    ProvideDeployKubernetesHelmChartTask::class.java)

            project.tasks.register(HelmBasedAzureAksStartDeployClusterTask.NAME,
                    HelmBasedAzureAksStartDeployClusterTask::class.java)
            project.tasks.register(HelmBasedAzureAksInstallDeployClusterTask.NAME,
                    HelmBasedAzureAksInstallDeployClusterTask::class.java)
            project.tasks.register(HelmBasedAzureAksStopDeployClusterTask.NAME,
                    HelmBasedAzureAksStopDeployClusterTask::class.java)

            project.tasks.register(HelmBasedAwsEksStartDeployClusterTask.NAME,
                    HelmBasedAwsEksStartDeployClusterTask::class.java)
            project.tasks.register(HelmBasedAwsEksInstallDeployClusterTask.NAME,
                    HelmBasedAwsEksInstallDeployClusterTask::class.java)
            project.tasks.register(HelmBasedAwsEksStopDeployClusterTask.NAME,
                    HelmBasedAwsEksStopDeployClusterTask::class.java)

            project.tasks.register(HelmBasedGcpGkeStartDeployClusterTask.NAME,
                    HelmBasedGcpGkeStartDeployClusterTask::class.java)
            project.tasks.register(HelmBasedGcpGkeInstallDeployClusterTask.NAME,
                    HelmBasedGcpGkeInstallDeployClusterTask::class.java)
            project.tasks.register(HelmBasedGcpGkeStopDeployClusterTask.NAME,
                    HelmBasedGcpGkeStopDeployClusterTask::class.java)

            project.tasks.register(HelmBasedAwsOpenShiftStartDeployClusterTask.NAME,
                    HelmBasedAwsOpenShiftStartDeployClusterTask::class.java)
            project.tasks.register(HelmBasedAwsOpenShiftInstallDeployClusterTask.NAME,
                    HelmBasedAwsOpenShiftInstallDeployClusterTask::class.java)
            project.tasks.register(HelmBasedAwsOpenShiftStopDeployClusterTask.NAME,
                    HelmBasedAwsOpenShiftStopDeployClusterTask::class.java)

            project.tasks.register(HelmBasedOnPremStartDeployClusterTask.NAME,
                    HelmBasedOnPremStartDeployClusterTask::class.java)
            project.tasks.register(HelmBasedOnPremInstallDeployClusterTask.NAME,
                    HelmBasedOnPremInstallDeployClusterTask::class.java)
            project.tasks.register(HelmBasedOnPremStopDeployClusterTask.NAME,
                    HelmBasedOnPremStopDeployClusterTask::class.java)


            // Cluster Terraform
            project.tasks.register(TerraformBasedAwsEksStartDeployClusterTask.NAME,
                TerraformBasedAwsEksStartDeployClusterTask::class.java)
            project.tasks.register(TerraformBasedAwsEksStopDeployClusterTask.NAME,
                TerraformBasedAwsEksStopDeployClusterTask::class.java)

            // Cluster Docker Compose
            project.tasks.register(DockerComposeBasedStartDeployClusterTask.NAME,
                DockerComposeBasedStartDeployClusterTask::class.java)
            project.tasks.register(DockerComposeBasedStopDeployClusterTask.NAME,
                DockerComposeBasedStopDeployClusterTask::class.java)

            //Database
            project.tasks.register(DatabaseStartTask.NAME, DatabaseStartTask::class.java)
            project.tasks.register(DatabaseStopTask.NAME, DatabaseStopTask::class.java)

            //Deploy Server
            project.tasks.register(ApplicationConfigurationOverrideTask.NAME,
                ApplicationConfigurationOverrideTask::class.java)
            project.tasks.register(CentralConfigurationTask.NAME, CentralConfigurationTask::class.java)
            project.tasks.register(CheckUILibVersionsTask.NAME, CheckUILibVersionsTask::class.java)
            project.tasks.register(CopyServerBuildArtifactsTask.NAME, CopyServerBuildArtifactsTask::class.java)
            project.tasks.register(CopyServerFoldersTask.NAME, CopyServerFoldersTask::class.java)
            project.tasks.register(ServerCopyOverlaysTask.NAME, ServerCopyOverlaysTask::class.java)
            project.tasks.register(DockerBasedStopDeployTask.NAME, DockerBasedStopDeployTask::class.java)
            project.tasks.register(DownloadAndExtractDbUnitDataDistTask.NAME,
                DownloadAndExtractDbUnitDataDistTask::class.java)
            project.tasks.register(DownloadAndExtractServerDistTask.NAME, DownloadAndExtractServerDistTask::class.java)
            project.tasks.register(ExportDatabaseTask.NAME, ExportDatabaseTask::class.java)
            project.tasks.register(GenerateSecurePekkoKeysTask.NAME, GenerateSecurePekkoKeysTask::class.java)
            project.tasks.register(ImportDbUnitDataTask.NAME, ImportDbUnitDataTask::class.java)
            project.tasks.register(PrepareDatabaseTask.NAME, PrepareDatabaseTask::class.java)
            project.tasks.register(PrepareServerTask.NAME, PrepareServerTask::class.java)
            project.tasks.register(RunDatasetGenerationTask.NAME, RunDatasetGenerationTask::class.java)
            project.tasks.register(RunDevOpsAsCodeTask.NAME, RunDevOpsAsCodeTask::class.java)
            project.tasks.register(SetServerLogbackLevelsTask.NAME, SetServerLogbackLevelsTask::class.java)
            project.tasks.register(ServerYamlPatchTask.NAME, ServerYamlPatchTask::class.java)
            project.tasks.register(StartDeployServerInstanceTask.NAME, StartDeployServerInstanceTask::class.java)
            project.tasks.register(TlsApplicationConfigurationOverrideTask.NAME,
                TlsApplicationConfigurationOverrideTask::class.java)

            //Infrastructure
            project.tasks.register(GitlabStartTask.NAME, GitlabStartTask::class.java)
            project.tasks.register(GitlabStopTask.NAME, GitlabStopTask::class.java)
            project.tasks.register(InfrastructureStopTask.NAME, InfrastructureStopTask::class.java)
            project.tasks.register(InfrastructureStartTask.NAME, InfrastructureStartTask::class.java)

            //Integration Server
            project.tasks.register(ShutdownDeployIntegrationServerTask.NAME,
                ShutdownDeployIntegrationServerTask::class.java)
            project.tasks.register(StartDeployIntegrationServerTask.NAME, StartDeployIntegrationServerTask::class.java).configure {
                dependsOn(itcfg)
            }

            //Maintenance
            project.tasks.register(CleanupBeforeStartupTask.NAME, CleanupBeforeStartupTask::class.java)

            //MQ
            project.tasks.register(ShutdownMqTask.NAME, ShutdownMqTask::class.java)
            project.tasks.register(StartMqTask.NAME, StartMqTask::class.java)

            //Plugin Manager
            project.tasks.register(StartPluginManagerTask.NAME, StartPluginManagerTask::class.java)

            //Satellite
            project.tasks.register(DownloadAndExtractSatelliteDistTask.NAME,
                DownloadAndExtractSatelliteDistTask::class.java)
            project.tasks.register(PrepareSatellitesTask.NAME, PrepareSatellitesTask::class.java)
            project.tasks.register(SatelliteOverlaysTask.NAME, SatelliteOverlaysTask::class.java)
            project.tasks.register(SatelliteSyncPluginsTask.NAME, SatelliteSyncPluginsTask::class.java)
            project.tasks.register(ShutdownSatelliteTask.NAME, ShutdownSatelliteTask::class.java)
            project.tasks.register(StartSatelliteTask.NAME, StartSatelliteTask::class.java)

            //Workers
            project.tasks.register(CopyIntegrationServerTask.NAME, CopyIntegrationServerTask::class.java)
            project.tasks.register(SyncServerPluginsWithWorkerTask.NAME, SyncServerPluginsWithWorkerTask::class.java)
            project.tasks.register(DownloadAndExtractWorkerDistTask.NAME, DownloadAndExtractWorkerDistTask::class.java)
            project.tasks.register(SetWorkersLogbackLevelsTask.NAME, SetWorkersLogbackLevelsTask::class.java)
            project.tasks.register(ShutdownWorkersTask.NAME, ShutdownWorkersTask::class.java)
            project.tasks.register(StartWorkersTask.NAME, StartWorkersTask::class.java)
            project.tasks.register(WorkerOverlaysTask.NAME, WorkerOverlaysTask::class.java)
            project.tasks.register(PrepareWorkersTask.NAME, PrepareWorkersTask::class.java)

            //Central configuration service
            project.tasks.register(DownloadAndExtractCentralConfigurationServerDistTask.NAME, DownloadAndExtractCentralConfigurationServerDistTask::class.java)
            project.tasks.register(PrepareCentralConfigurationServerTask.NAME, PrepareCentralConfigurationServerTask::class.java)
            project.tasks.register(CentralConfigurationServerOverlaysTask.NAME, CentralConfigurationServerOverlaysTask::class.java)
            project.tasks.register(CentralConfigurationServerYamlPatchTask.NAME, CentralConfigurationServerYamlPatchTask::class.java)
            project.tasks.register(StartCentralConfigurationServerTask.NAME, StartCentralConfigurationServerTask::class.java)
            project.tasks.register(ShutdownCentralConfigurationServerTask.NAME, ShutdownCentralConfigurationServerTask::class.java)

            //Tests
            project.tasks.register(IntegrationTestsTask.NAME, IntegrationTestsTask::class.java)
        }
    }
}
