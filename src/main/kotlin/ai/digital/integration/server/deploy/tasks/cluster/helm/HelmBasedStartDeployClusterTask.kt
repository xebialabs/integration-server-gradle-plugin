package ai.digital.integration.server.deploy.tasks.cluster.helm

import ai.digital.integration.server.common.constant.OperatorHelmProviderName
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import ai.digital.integration.server.deploy.tasks.cli.DownloadAndExtractCliDistTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.awseks.HelmBasedAwsEksStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.helm.onprem.HelmBasedOnPremStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.awseks.OperatorBasedAwsEksStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.awsopenshift.OperatorBasedAwsOpenShiftStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.azureaks.OperatorBasedAzureAksStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.gcpgke.OperatorBasedGcpGkeStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.onprem.OperatorBasedOnPremStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.vmwareopenshift.OperatorBasedVmWareOpenShiftStartDeployClusterTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class HelmBasedStartDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "helmBasedStartDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        if (DeployExtensionUtil.getExtension(project).clusterProfiles.operator().activeProviderName.isPresent) {
            this.dependsOn(
                DownloadAndExtractCliDistTask.NAME,
                when (val providerName = DeployClusterUtil.getHelmProvider(project)) {
                    OperatorHelmProviderName.AWS_EKS.providerName ->
                        HelmBasedAwsEksStartDeployClusterTask.NAME
                   /* OperatorHelmProviderName.AWS_OPENSHIFT.providerName ->
                        OperatorBasedAwsOpenShiftStartDeployClusterTask.NAME
                    OperatorHelmProviderName.AZURE_AKS.providerName ->
                        OperatorBasedAzureAksStartDeployClusterTask.NAME
                    OperatorHelmProviderName.GCP_GKE.providerName ->
                        OperatorBasedGcpGkeStartDeployClusterTask.NAME*/
                    OperatorHelmProviderName.ON_PREMISE.providerName ->
                        HelmBasedOnPremStartDeployClusterTask.NAME
                    /*OperatorHelmProviderName.VMWARE_OPENSHIFT.providerName ->
                        OperatorBasedVmWareOpenShiftStartDeployClusterTask.NAME*/
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
            project.logger.warn("Active provider name is not set - HelmBasedStartDeployClusterTask")
        }
    }

    @TaskAction
    fun launch() {
        val providerName = DeployClusterUtil.getHelmProvider(project)
        project.logger.lifecycle("Helm based Deploy Cluster with provider $providerName has started.")
    }
}
