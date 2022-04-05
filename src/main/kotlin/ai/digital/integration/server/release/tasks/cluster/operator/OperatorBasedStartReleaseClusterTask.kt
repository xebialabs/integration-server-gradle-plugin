package ai.digital.integration.server.release.tasks.cluster.operator

import ai.digital.integration.server.common.constant.OperatorProviderName
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.release.internals.ReleaseExtensionUtil
import ai.digital.integration.server.release.tasks.cluster.ReleaseClusterUtil
import ai.digital.integration.server.release.tasks.cluster.operator.awseks.OperatorBasedAwsEksStartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.awsopenshift.OperatorBasedAwsOpenShiftStartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.azureaks.OperatorBasedAzureAksStartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.gcpgke.OperatorBasedGcpGkeStartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.onprem.OperatorBasedOnPremStartReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.vmwareopenshift.OperatorBasedVmWareOpenShiftStartReleaseClusterTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedStartReleaseClusterTask : DefaultTask() {

    companion object {
        const val NAME = "operatorBasedStartReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        if (ReleaseExtensionUtil.getExtension(project).clusterProfiles.operator().activeProviderName.isPresent) {
            this.dependsOn(when (val providerName = ReleaseClusterUtil.getOperatorProvider(project)) {
                OperatorProviderName.AWS_EKS.providerName ->
                    OperatorBasedAwsEksStartReleaseClusterTask.NAME
                OperatorProviderName.AWS_OPENSHIFT.providerName ->
                    OperatorBasedAwsOpenShiftStartReleaseClusterTask.NAME
                OperatorProviderName.AZURE_AKS.providerName ->
                    OperatorBasedAzureAksStartReleaseClusterTask.NAME
                OperatorProviderName.GCP_GKE.providerName ->
                    OperatorBasedGcpGkeStartReleaseClusterTask.NAME
                OperatorProviderName.ON_PREMISE.providerName ->
                    OperatorBasedOnPremStartReleaseClusterTask.NAME
                OperatorProviderName.VMWARE_OPENSHIFT.providerName ->
                    OperatorBasedVmWareOpenShiftStartReleaseClusterTask.NAME
                else -> {
                    throw IllegalArgumentException("Provided operator provider name `$providerName` is not supported. Choose one of ${
                        OperatorProviderName.values().joinToString()
                    }")
                }
            })
        } else {
            project.logger.warn("Active provider name is not set - OperatorBasedStartReleaseClusterTask")
        }
    }

    @TaskAction
    fun launch() {
        val providerName = ReleaseClusterUtil.getOperatorProvider(project)
        project.logger.lifecycle("Operator based Release Cluster with provider $providerName has started.")
    }
}
