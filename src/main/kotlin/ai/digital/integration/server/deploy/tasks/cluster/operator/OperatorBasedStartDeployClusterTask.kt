package ai.digital.integration.server.deploy.tasks.cluster.operator

import ai.digital.integration.server.common.constant.OperatorHelmProviderName
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import ai.digital.integration.server.deploy.tasks.cli.DownloadAndExtractCliDistTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.awseks.OperatorBasedAwsEksStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.awsopenshift.OperatorBasedAwsOpenShiftStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.azureaks.OperatorBasedAzureAksStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.gcpgke.OperatorBasedGcpGkeStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.onprem.OperatorBasedOnPremStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.vmwareopenshift.OperatorBasedVmWareOpenShiftStartDeployClusterTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedStartDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "operatorBasedStartDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        if (DeployExtensionUtil.getExtension(project).clusterProfiles.operator().activeProviderName.isPresent) {
            this.dependsOn(
                DownloadAndExtractCliDistTask.NAME,
                when (val providerName = DeployClusterUtil.getOperatorProvider(project)) {
                    OperatorHelmProviderName.AWS_EKS.providerName ->
                        OperatorBasedAwsEksStartDeployClusterTask.NAME
                    OperatorHelmProviderName.AWS_OPENSHIFT.providerName ->
                        OperatorBasedAwsOpenShiftStartDeployClusterTask.NAME
                    OperatorHelmProviderName.AZURE_AKS.providerName ->
                        OperatorBasedAzureAksStartDeployClusterTask.NAME
                    OperatorHelmProviderName.GCP_GKE.providerName ->
                        OperatorBasedGcpGkeStartDeployClusterTask.NAME
                    OperatorHelmProviderName.ON_PREMISE.providerName ->
                        OperatorBasedOnPremStartDeployClusterTask.NAME
                    OperatorHelmProviderName.VMWARE_OPENSHIFT.providerName ->
                        OperatorBasedVmWareOpenShiftStartDeployClusterTask.NAME
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
            project.logger.warn("Active provider name is not set - OperatorBasedStartDeployClusterTask")
        }
    }

    @TaskAction
    fun launch() {
        val providerName = DeployClusterUtil.getOperatorProvider(project)
        project.logger.lifecycle("Operator based Deploy Cluster with provider $providerName has started.")
    }
}
