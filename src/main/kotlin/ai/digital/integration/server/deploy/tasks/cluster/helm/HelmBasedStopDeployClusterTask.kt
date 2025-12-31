package ai.digital.integration.server.deploy.tasks.cluster.helm

import ai.digital.integration.server.common.constant.OperatorHelmProviderName
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import ai.digital.integration.server.deploy.tasks.cluster.helm.awseks.HelmBasedAwsEksStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.awsopenshift.HelmBasedAwsOpenShiftStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.azureaks.HelmBasedAzureAksStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.gcpgke.HelmBasedGcpGkeStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.onprem.HelmBasedOnPremStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.awseks.OperatorBasedAwsEksStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.awsopenshift.OperatorBasedAwsOpenShiftStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.azureaks.OperatorBasedAzureAksStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.gcpgke.OperatorBasedGcpGkeStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.onprem.OperatorBasedOnPremStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.vmwareopenshift.OperatorBasedVmWareOpenShiftStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.server.operator.StopDeployServerForOperatorInstanceTask
import ai.digital.integration.server.deploy.tasks.server.operator.StopDeployServerForOperatorUpgradeTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class HelmBasedStopDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "helmBasedStopDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        // Configure dependencies directly - no afterEvaluate needed in Gradle 9
        if (DeployExtensionUtil.getExtension(project).clusterProfiles.helm().activeProviderName.isPresent) {
            dependsOn(
                when (val providerName = DeployClusterUtil.getHelmProvider(project)) {
                    OperatorHelmProviderName.AWS_EKS.providerName ->
                        HelmBasedAwsEksStopDeployClusterTask.NAME
                    OperatorHelmProviderName.AWS_OPENSHIFT.providerName ->
                        HelmBasedAwsOpenShiftStopDeployClusterTask.NAME
                    OperatorHelmProviderName.AZURE_AKS.providerName ->
                        HelmBasedAzureAksStopDeployClusterTask.NAME
                    OperatorHelmProviderName.GCP_GKE.providerName ->
                        HelmBasedGcpGkeStopDeployClusterTask.NAME
                    OperatorHelmProviderName.ON_PREMISE.providerName ->
                        HelmBasedOnPremStopDeployClusterTask.NAME
                    /*OperatorHelmProviderName.VMWARE_OPENSHIFT.providerName ->
                    OperatorBasedVmWareOpenShiftStopDeployClusterTask.NAME*/
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
            project.logger.warn("Active provider name is not set - OperatorBasedStopDeployClusterTask")
        }
    }

    @TaskAction
    fun launch() {
        val providerName = DeployClusterUtil.getHelmProvider(project)
        project.logger.lifecycle("Operator based Deploy Cluster with provider $providerName  is about to stop.")
    }
}
