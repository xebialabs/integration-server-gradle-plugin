package ai.digital.integration.server.release.tasks.cluster.operator

import ai.digital.integration.server.common.constant.OperatorHelmProviderName
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.release.tasks.cluster.ReleaseClusterUtil
import ai.digital.integration.server.release.tasks.cluster.operator.awseks.OperatorBasedAwsEksInstallReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.awsopenshift.OperatorBasedAwsOpenShiftInstallReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.azureaks.OperatorBasedAzureAksInstallReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.gcpgke.OperatorBasedGcpGkeInstallReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.onprem.OperatorBasedOnPremInstallReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.vmwareopenshift.OperatorBasedVmWareOpenShiftInstallReleaseClusterTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedInstallReleaseClusterTask : DefaultTask() {

    companion object {
        const val NAME = "operatorBasedInstallReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        this.dependsOn(when (val providerName = ReleaseClusterUtil.getOperatorProvider(project)) {
            OperatorHelmProviderName.AWS_EKS.providerName ->
                OperatorBasedAwsEksInstallReleaseClusterTask.NAME
            OperatorHelmProviderName.AWS_OPENSHIFT.providerName ->
                OperatorBasedAwsOpenShiftInstallReleaseClusterTask.NAME
            OperatorHelmProviderName.AZURE_AKS.providerName ->
                OperatorBasedAzureAksInstallReleaseClusterTask.NAME
            OperatorHelmProviderName.GCP_GKE.providerName ->
                OperatorBasedGcpGkeInstallReleaseClusterTask.NAME
            OperatorHelmProviderName.ON_PREMISE.providerName ->
                OperatorBasedOnPremInstallReleaseClusterTask.NAME
            OperatorHelmProviderName.VMWARE_OPENSHIFT.providerName ->
                OperatorBasedVmWareOpenShiftInstallReleaseClusterTask.NAME
            else -> {
                throw IllegalArgumentException("Provided operator provider name `$providerName` is not supported. Choose one of ${
                    OperatorHelmProviderName.values().joinToString()
                }")
            }
        })
    }

    @TaskAction
    fun launch() {
        val providerName = ReleaseClusterUtil.getOperatorProvider(project)
        project.logger.lifecycle("Operator based Release Cluster with provider $providerName has started.")
    }
}
