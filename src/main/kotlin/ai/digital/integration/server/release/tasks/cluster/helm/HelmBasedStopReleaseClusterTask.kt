package ai.digital.integration.server.release.tasks.cluster.helm

import ai.digital.integration.server.common.constant.OperatorHelmProviderName
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.tasks.cli.DownloadAndExtractCliDistTask
import ai.digital.integration.server.deploy.tasks.server.operator.StopDeployServerForOperatorInstanceTask
import ai.digital.integration.server.deploy.tasks.server.operator.StopDeployServerForOperatorUpgradeTask
import ai.digital.integration.server.release.internals.ReleaseExtensionUtil
import ai.digital.integration.server.release.tasks.cluster.ReleaseClusterUtil
import ai.digital.integration.server.release.tasks.cluster.helm.awseks.HelmBasedAwsEksStopReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.awsopenshift.HelmBasedAwsOpenShiftStopReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.azureaks.HelmBasedAzureAksStopReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.gcpgke.HelmBasedGcpGkeStopReleaseClusterTask
import ai.digital.integration.server.release.tasks.cluster.helm.onprem.HelmBasedOnPremStopReleaseClusterTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class HelmBasedStopReleaseClusterTask : DefaultTask() {

    companion object {
        const val NAME = "helmBasedStopReleaseCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        // Configure dependencies directly - no afterEvaluate needed in Gradle 9
            if (ReleaseExtensionUtil.getExtension(project).clusterProfiles.helm().activeProviderName.isPresent) {
                dependsOn(
                    DownloadAndExtractCliDistTask.NAME,
                    when (val providerName = ReleaseClusterUtil.getHelmProvider(project)) {
                        OperatorHelmProviderName.AWS_EKS.providerName ->
                            HelmBasedAwsEksStopReleaseClusterTask.NAME
                        OperatorHelmProviderName.AWS_OPENSHIFT.providerName ->
                            HelmBasedAwsOpenShiftStopReleaseClusterTask.NAME
                        OperatorHelmProviderName.AZURE_AKS.providerName ->
                            HelmBasedAzureAksStopReleaseClusterTask.NAME
                        OperatorHelmProviderName.GCP_GKE.providerName ->
                            HelmBasedGcpGkeStopReleaseClusterTask.NAME
                        OperatorHelmProviderName.ON_PREMISE.providerName ->
                            HelmBasedOnPremStopReleaseClusterTask.NAME
                        /*OperatorHelmProviderName.VMWARE_OPENSHIFT.providerName ->
                        OperatorBasedVmWareOpenShiftStopReleaseClusterTask.NAME*/
                        else -> {
                            throw IllegalArgumentException(
                                "Provided helm provider name `$providerName` is not supported. Choose one of ${
                                    OperatorHelmProviderName.values().joinToString()
                                }"
                            )
                        }
                    }
                )
            } else {
                project.logger.warn("Active helm name is not set - HelmBasedStopReleaseClusterTask")
            }
    }

    @TaskAction
    fun launch() {
        val providerName = ReleaseClusterUtil.getOperatorProvider(project)
        project.logger.lifecycle("Helm based Release Cluster with provider $providerName  is about to stop.")
    }
}
