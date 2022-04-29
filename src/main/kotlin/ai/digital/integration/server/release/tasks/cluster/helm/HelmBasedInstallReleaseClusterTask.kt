package ai.digital.integration.server.release.tasks.cluster.helm

import ai.digital.integration.server.common.constant.OperatorHelmProviderName
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.release.internals.ReleaseExtensionUtil
import ai.digital.integration.server.release.tasks.cluster.ReleaseClusterUtil
import ai.digital.integration.server.release.tasks.cluster.helm.awseks.HelmBasedAwsEksInstallReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.awsopenshift.HelmBasedAwsOpenShiftInstallReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.azureaks.HelmBasedAzureAksInstallReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.gcpgke.HelmBasedGcpGkeInstallReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.onprem.HelmBasedOnPremInstallReleaseClusterTask

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class HelmBasedInstallReleaseClusterTask : DefaultTask() {

    companion object {
        const val NAME = "helmBasedInstallReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        if (ReleaseExtensionUtil.getExtension(project).clusterProfiles.helm().activeProviderName.isPresent) {
            this.dependsOn(when (val providerName = ReleaseClusterUtil.getHelmProvider(project)) {
                OperatorHelmProviderName.AWS_EKS.providerName ->
                    HelmBasedAwsEksInstallReleaseClusterTask.NAME
                OperatorHelmProviderName.AWS_OPENSHIFT.providerName ->
                    HelmBasedAwsOpenShiftInstallReleaseClusterTask.NAME
                OperatorHelmProviderName.AZURE_AKS.providerName ->
                    HelmBasedAzureAksInstallReleaseClusterTask.NAME
                OperatorHelmProviderName.GCP_GKE.providerName ->
                    HelmBasedGcpGkeInstallReleaseClusterTask.NAME
                OperatorHelmProviderName.ON_PREMISE.providerName ->
                    HelmBasedOnPremInstallReleaseClusterTask.NAME
                /*OperatorHelmProviderName.VMWARE_OPENSHIFT.providerName ->
                    OperatorBasedVmWareOpenShiftInstallReleaseClusterTask.NAME*/
                else -> {
                    throw IllegalArgumentException("Provided helm provider name `$providerName` is not supported. Choose one of ${
                        OperatorHelmProviderName.values().joinToString()
                    }")
                }
            })
        } else {
            project.logger.warn("Active provider name is not set - OperatorBasedInstallReleaseClusterTask")
        }
    }

    @TaskAction
    fun launch() {
        val providerName = ReleaseClusterUtil.getHelmProvider(project)
        project.logger.lifecycle("Operator based Release Cluster with provider $providerName has started.")
    }
}
