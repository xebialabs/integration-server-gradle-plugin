package ai.digital.integration.server.deploy.tasks.cluster.operator

import ai.digital.integration.server.common.constant.OperatorHelmProviderName
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import ai.digital.integration.server.deploy.tasks.cli.DownloadAndExtractCliDistTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.awseks.OperatorBasedAwsEksInstallDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.awsopenshift.OperatorBasedAwsOpenShiftInstallDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.azureaks.OperatorBasedAzureAksInstallDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.gcpgke.OperatorBasedGcpGkeInstallDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.onprem.OperatorBasedOnPremInstallDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.vmwareopenshift.OperatorBasedVmWareOpenShiftInstallDeployClusterTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedInstallDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "operatorBasedInstallDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        if (DeployExtensionUtil.getExtension(project).clusterProfiles.operator().activeProviderName.isPresent) {
            this.dependsOn(
                DownloadAndExtractCliDistTask.NAME,
                when (val providerName = DeployClusterUtil.getOperatorProvider(project)) {
                    OperatorHelmProviderName.AWS_EKS.providerName ->
                    OperatorBasedAwsEksInstallDeployClusterTask.NAME
                    OperatorHelmProviderName.AWS_OPENSHIFT.providerName ->
                    OperatorBasedAwsOpenShiftInstallDeployClusterTask.NAME
                    OperatorHelmProviderName.AZURE_AKS.providerName ->
                    OperatorBasedAzureAksInstallDeployClusterTask.NAME
                    OperatorHelmProviderName.GCP_GKE.providerName ->
                    OperatorBasedGcpGkeInstallDeployClusterTask.NAME
                    OperatorHelmProviderName.ON_PREMISE.providerName ->
                    OperatorBasedOnPremInstallDeployClusterTask.NAME
                    OperatorHelmProviderName.VMWARE_OPENSHIFT.providerName ->
                    OperatorBasedVmWareOpenShiftInstallDeployClusterTask.NAME
                else -> {
                    throw IllegalArgumentException("Provided operator provider name `$providerName` is not supported. Choose one of ${
                        OperatorHelmProviderName.values().joinToString()
                    }")
                }
            })
        } else {
            project.logger.warn("Active provider name is not set - OperatorBasedInstallDeployClusterTask")
        }
    }

    @TaskAction
    fun launch() {
        val providerName = DeployClusterUtil.getOperatorProvider(project)
        project.logger.lifecycle("Operator based Deploy Cluster with provider $providerName has started.")
    }
}
