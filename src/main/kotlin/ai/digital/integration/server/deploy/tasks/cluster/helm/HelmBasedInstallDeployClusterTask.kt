package ai.digital.integration.server.deploy.tasks.cluster.helm

import ai.digital.integration.server.common.constant.OperatorHelmProviderName
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import ai.digital.integration.server.deploy.tasks.cli.DownloadAndExtractCliDistTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.awseks.HelmBasedAwsEksInstallDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.onprem.HelmBasedOnPremInstallDeployClusterTask

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class HelmBasedInstallDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "helmBasedInstallDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        if (DeployExtensionUtil.getExtension(project).clusterProfiles.helm().activeProviderName.isPresent) {
            this.dependsOn(
                DownloadAndExtractCliDistTask.NAME,
                when (val providerName = DeployClusterUtil.getHelmProvider(project)) {
                    OperatorHelmProviderName.AWS_EKS.providerName ->
                    HelmBasedAwsEksInstallDeployClusterTask.NAME
                   /* OperatorHelmProviderName.AWS_OPENSHIFT.providerName ->
                    OperatorBasedAwsOpenShiftInstallDeployClusterTask.NAME
                    OperatorHelmProviderName.AZURE_AKS.providerName ->
                    OperatorBasedAzureAksInstallDeployClusterTask.NAME
                    OperatorHelmProviderName.GCP_GKE.providerName ->
                    OperatorBasedGcpGkeInstallDeployClusterTask.NAME*/
                    OperatorHelmProviderName.ON_PREMISE.providerName ->
                    HelmBasedOnPremInstallDeployClusterTask.NAME
                    /*OperatorHelmProviderName.VMWARE_OPENSHIFT.providerName ->
                    OperatorBasedVmWareOpenShiftInstallDeployClusterTask.NAME*/
                else -> {
                    throw IllegalArgumentException("Provided helm provider name `$providerName` is not supported. Choose one of ${
                        OperatorHelmProviderName.values().joinToString()
                    }")
                }
            })
        } else {
            project.logger.warn("Active provider name is not set - HelmBasedInstallDeployClusterTask")
        }
    }

    @TaskAction
    fun launch() {
        val providerName = DeployClusterUtil.getHelmProvider(project)
        project.logger.lifecycle("Helm based Deploy Cluster with provider $providerName has started.")
    }
}
