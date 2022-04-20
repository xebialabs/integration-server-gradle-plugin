package ai.digital.integration.server.release.tasks.cluster.helm

import ai.digital.integration.server.common.constant.OperatorHelmProviderName
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.release.internals.ReleaseExtensionUtil
import ai.digital.integration.server.release.tasks.cluster.ReleaseClusterUtil
import ai.digital.integration.server.release.tasks.cluster.helm.awseks.HelmBasedAwsEksStartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.awsopenshift.HelmBasedAwsOpenShiftStartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.azureaks.HelmBasedAzureAksStartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.gcpgke.HelmBasedGcpGkeStartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.onprem.HelmBasedOnPremStartReleaseClusterTask

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class HelmBasedStartReleaseClusterTask : DefaultTask() {

    companion object {
        const val NAME = "helmBasedStartReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        if (ReleaseExtensionUtil.getExtension(project).clusterProfiles.helm().activeProviderName.isPresent) {
            this.dependsOn(when (val providerName = ReleaseClusterUtil.getHelmProvider(project)) {
                OperatorHelmProviderName.AWS_EKS.providerName ->
                    HelmBasedAwsEksStartReleaseClusterTask.NAME
                OperatorHelmProviderName.AWS_OPENSHIFT.providerName ->
                    HelmBasedAwsOpenShiftStartReleaseClusterTask.NAME
                OperatorHelmProviderName.AZURE_AKS.providerName ->
                    HelmBasedAzureAksStartReleaseClusterTask.NAME
                OperatorHelmProviderName.GCP_GKE.providerName ->
                    HelmBasedGcpGkeStartReleaseClusterTask.NAME
                OperatorHelmProviderName.ON_PREMISE.providerName ->
                    HelmBasedOnPremStartReleaseClusterTask.NAME
                /*OperatorHelmProviderName.VMWARE_OPENSHIFT.providerName ->
                    OperatorBasedVmWareOpenShiftStartReleaseClusterTask.NAME*/
                else -> {
                    throw IllegalArgumentException("Provided operator provider name `$providerName` is not supported. Choose one of ${
                        OperatorHelmProviderName.values().joinToString()
                    }")
                }
            })
        } else {
            project.logger.warn("Active provider name is not set - OperatorBasedStartReleaseClusterTask")
        }
    }

    @TaskAction
    fun launch() {
        val providerName = ReleaseClusterUtil.getHelmProvider(project)
        project.logger.lifecycle("Operator based Release Cluster with provider $providerName has started.")
    }
}
