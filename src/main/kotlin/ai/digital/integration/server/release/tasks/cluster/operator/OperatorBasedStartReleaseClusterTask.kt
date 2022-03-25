package ai.digital.integration.server.release.tasks.cluster.operator

import ai.digital.integration.server.common.constant.OperatorHelmProviderName
import ai.digital.integration.server.common.constant.PluginConstant
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

        this.dependsOn(when (val providerName = ReleaseClusterUtil.getOperatorProvider(project)) {
            OperatorHelmProviderName.AWS_EKS.providerName ->
                OperatorBasedAwsEksStartReleaseClusterTask.NAME
            OperatorHelmProviderName.AWS_OPENSHIFT.providerName ->
                OperatorBasedAwsOpenShiftStartReleaseClusterTask.NAME
            OperatorHelmProviderName.AZURE_AKS.providerName ->
                OperatorBasedAzureAksStartReleaseClusterTask.NAME
            OperatorHelmProviderName.GCP_GKE.providerName ->
                OperatorBasedGcpGkeStartReleaseClusterTask.NAME
            OperatorHelmProviderName.ON_PREMISE.providerName ->
                OperatorBasedOnPremStartReleaseClusterTask.NAME
            OperatorHelmProviderName.VMWARE_OPENSHIFT.providerName ->
                OperatorBasedVmWareOpenShiftStartReleaseClusterTask.NAME
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
