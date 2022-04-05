package ai.digital.integration.server.release.tasks.cluster.operator

import ai.digital.integration.server.common.constant.OperatorProviderName
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.release.internals.ReleaseExtensionUtil
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

        if (ReleaseExtensionUtil.getExtension(project).clusterProfiles.operator().activeProviderName.isPresent) {
            this.dependsOn(when (val providerName = ReleaseClusterUtil.getOperatorProvider(project)) {
                OperatorProviderName.AWS_EKS.providerName ->
                    OperatorBasedAwsEksInstallReleaseClusterTask.NAME
                OperatorProviderName.AWS_OPENSHIFT.providerName ->
                    OperatorBasedAwsOpenShiftInstallReleaseClusterTask.NAME
                OperatorProviderName.AZURE_AKS.providerName ->
                    OperatorBasedAzureAksInstallReleaseClusterTask.NAME
                OperatorProviderName.GCP_GKE.providerName ->
                    OperatorBasedGcpGkeInstallReleaseClusterTask.NAME
                OperatorProviderName.ON_PREMISE.providerName ->
                    OperatorBasedOnPremInstallReleaseClusterTask.NAME
                OperatorProviderName.VMWARE_OPENSHIFT.providerName ->
                    OperatorBasedVmWareOpenShiftInstallReleaseClusterTask.NAME
                else -> {
                    throw IllegalArgumentException("Provided operator provider name `$providerName` is not supported. Choose one of ${
                        OperatorProviderName.values().joinToString()
                    }")
                }
            })
        } else {
            project.logger.warn("Active provider name is not set - OperatorBasedInstallReleaseClusterTask")
        }
    }

    @TaskAction
    fun launch() {
        val providerName = ReleaseClusterUtil.getOperatorProvider(project)
        project.logger.lifecycle("Operator based Release Cluster with provider $providerName has started.")
    }
}
