package ai.digital.integration.server.release.tasks.cluster.operator

import ai.digital.integration.server.common.constant.OperatorHelmProviderName
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.tasks.cli.DownloadAndExtractCliDistTask
import ai.digital.integration.server.deploy.tasks.server.operator.StopDeployServerForOperatorInstanceTask
import ai.digital.integration.server.deploy.tasks.server.operator.StopDeployServerForOperatorUpgradeTask
import ai.digital.integration.server.release.tasks.cluster.ReleaseClusterUtil
import ai.digital.integration.server.release.tasks.cluster.operator.awseks.OperatorBasedAwsEksStopReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.awsopenshift.OperatorBasedAwsOpenShiftStopReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.azureaks.OperatorBasedAzureAksStopReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.gcpgke.OperatorBasedGcpGkeStopReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.onprem.OperatorBasedOnPremStopReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.operator.vmwareopenshift.OperatorBasedVmWareOpenShiftStopReleaseClusterTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedStopReleaseClusterTask : DefaultTask() {

    companion object {
        const val NAME = "operatorBasedStopReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        this.dependsOn(
            DownloadAndExtractCliDistTask.NAME,
            when (val providerName = ReleaseClusterUtil.getOperatorProvider(project)) {
            OperatorHelmProviderName.AWS_EKS.providerName ->
                OperatorBasedAwsEksStopReleaseClusterTask.NAME
            OperatorHelmProviderName.AWS_OPENSHIFT.providerName ->
                OperatorBasedAwsOpenShiftStopReleaseClusterTask.NAME
            OperatorHelmProviderName.AZURE_AKS.providerName ->
                OperatorBasedAzureAksStopReleaseClusterTask.NAME
            OperatorHelmProviderName.GCP_GKE.providerName ->
                OperatorBasedGcpGkeStopReleaseClusterTask.NAME
            OperatorHelmProviderName.ON_PREMISE.providerName ->
                OperatorBasedOnPremStopReleaseClusterTask.NAME
            OperatorHelmProviderName.VMWARE_OPENSHIFT.providerName ->
                OperatorBasedVmWareOpenShiftStopReleaseClusterTask.NAME
            else -> {
                throw IllegalArgumentException("Provided operator provider name `$providerName` is not supported. Choose one of ${
                    OperatorHelmProviderName.values().joinToString()
                }")
            }
        })
        this.finalizedBy(
                StopDeployServerForOperatorInstanceTask.NAME,
                StopDeployServerForOperatorUpgradeTask.NAME
        )
    }

    @TaskAction
    fun launch() {
        val providerName = ReleaseClusterUtil.getOperatorProvider(project)
        project.logger.lifecycle("Operator based Release Cluster with provider $providerName  is about to stop.")
    }
}
