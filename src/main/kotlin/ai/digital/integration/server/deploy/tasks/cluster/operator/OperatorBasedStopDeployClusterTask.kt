package ai.digital.integration.server.deploy.tasks.cluster.operator

import ai.digital.integration.server.common.constant.OperatorHelmProviderName
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
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

open class OperatorBasedStopDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "operatorBasedStopDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        // Configure dependencies directly - no afterEvaluate needed in Gradle 9
        if (DeployExtensionUtil.getExtension(project).clusterProfiles.operator().activeProviderName.isPresent) {
            dependsOn(
                when (val providerName = DeployClusterUtil.getOperatorProvider(project)) {
                    OperatorHelmProviderName.AWS_EKS.providerName ->
                        OperatorBasedAwsEksStopDeployClusterTask.NAME
                    OperatorHelmProviderName.AWS_OPENSHIFT.providerName ->
                        OperatorBasedAwsOpenShiftStopDeployClusterTask.NAME
                    OperatorHelmProviderName.AZURE_AKS.providerName ->
                        OperatorBasedAzureAksStopDeployClusterTask.NAME
                    OperatorHelmProviderName.GCP_GKE.providerName ->
                        OperatorBasedGcpGkeStopDeployClusterTask.NAME
                    OperatorHelmProviderName.ON_PREMISE.providerName ->
                        OperatorBasedOnPremStopDeployClusterTask.NAME
                    OperatorHelmProviderName.VMWARE_OPENSHIFT.providerName ->
                        OperatorBasedVmWareOpenShiftStopDeployClusterTask.NAME
                    else -> {
                        throw IllegalArgumentException(
                            "Provided operator provider name `$providerName` is not supported. Choose one of ${
                                OperatorHelmProviderName.values().joinToString()
                            }"
                        )
                    }
                }
            )
        } else {
            project.logger.warn("Active provider name is not set - OperatorBasedStopDeployClusterTask")
        }
        finalizedBy(
            StopDeployServerForOperatorInstanceTask.NAME,
            StopDeployServerForOperatorUpgradeTask.NAME
        )
    }

    @TaskAction
    fun launch() {
        val providerName = DeployClusterUtil.getOperatorProvider(project)
        project.logger.lifecycle("Operator based Deploy Cluster with provider $providerName  is about to stop.")
    }
}
