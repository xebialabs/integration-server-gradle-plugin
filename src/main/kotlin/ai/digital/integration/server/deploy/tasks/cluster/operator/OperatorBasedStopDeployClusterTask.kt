package ai.digital.integration.server.deploy.tasks.cluster.operator

import ai.digital.integration.server.common.constant.OperatorProviderName
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import ai.digital.integration.server.deploy.tasks.cluster.operator.awseks.OperatorBasedAwsEksDeployClusterStopTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.awsopenshift.OperatorBasedAwsOpenShiftDeployClusterStopTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.azureaks.OperatorBasedAzureAksStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.gcpgke.OperatorBasedGcpGkeStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.onprem.OperatorBasedOnPremStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.vmwareopenshift.OperatorBasedVmWareOpenShiftStopDeployClusterTask
import ai.digital.integration.server.deploy.tasks.server.operator.StopDeployServerForOperatorInstanceTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class OperatorBasedStopDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "operatorBasedStopDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        this.dependsOn(when (val providerName = DeployClusterUtil.getOperatorProvider(project)) {
            OperatorProviderName.AWS_EKS.providerName ->
                OperatorBasedAwsEksDeployClusterStopTask.NAME
            OperatorProviderName.AWS_OPENSHIFT.providerName ->
                OperatorBasedAwsOpenShiftDeployClusterStopTask.NAME
            OperatorProviderName.AZURE_AKS.providerName ->
                OperatorBasedAzureAksStopDeployClusterTask.NAME
            OperatorProviderName.GCP_GKE.providerName ->
                OperatorBasedGcpGkeStopDeployClusterTask.NAME
            OperatorProviderName.ON_PREMISE.providerName ->
                OperatorBasedOnPremStopDeployClusterTask.NAME
            OperatorProviderName.VMWARE_OPENSHIFT.providerName ->
                OperatorBasedVmWareOpenShiftStopDeployClusterTask.NAME
            else -> {
                throw IllegalArgumentException("Provided operator provider name `$providerName` is not supported. Choose one of ${
                    OperatorProviderName.values().joinToString()
                }")
            }
        })
        this.finalizedBy(StopDeployServerForOperatorInstanceTask.NAME)
    }

    @TaskAction
    fun launch() {
        val providerName = DeployClusterUtil.getOperatorProvider(project)
        project.logger.lifecycle("Operator based Deploy Cluster with provider $providerName  is about to stop.")
    }
}
