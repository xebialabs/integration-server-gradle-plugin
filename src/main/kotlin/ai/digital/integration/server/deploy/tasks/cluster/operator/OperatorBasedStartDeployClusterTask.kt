package ai.digital.integration.server.deploy.tasks.cluster.operator

import ai.digital.integration.server.common.constant.OperatorProviderName
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import ai.digital.integration.server.deploy.tasks.cluster.operator.awseks.OperatorBasedAwsEksStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.awsopenshift.OperatorBasedAwsOpenShiftStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.azureaks.OperatorBasedAzureAksStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.gcpgke.OperatorBasedGcpGkeStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.onprem.OperatorBasedOnPremStartDeployClusterTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.vmwareopenshift.OperatorBasedVmWareOpenShiftStartDeployClusterTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.impldep.org.eclipse.jgit.errors.NotSupportedException

open class OperatorBasedStartDeployClusterTask : DefaultTask() {

    companion object {
        const val NAME = "operatorBasedStartDeployCluster"
    }

    init {
        group = PluginConstant.PLUGIN_GROUP

        this.dependsOn(when (val providerName = DeployClusterUtil.getOperatorProvider(project)) {
            OperatorProviderName.AWS_EKS.providerName ->
                OperatorBasedAwsEksStartDeployClusterTask.NAME
            OperatorProviderName.AWS_OPENSHIFT.providerName ->
                OperatorBasedAwsOpenShiftStartDeployClusterTask.NAME
            OperatorProviderName.AZURE_AKS.providerName ->
                OperatorBasedAzureAksStartDeployClusterTask.NAME
            OperatorProviderName.GCP_GKE.providerName ->
                OperatorBasedGcpGkeStartDeployClusterTask.NAME
            OperatorProviderName.ON_PREMISE.providerName ->
                OperatorBasedOnPremStartDeployClusterTask.NAME
            OperatorProviderName.VMWARE_OPENSHIFT.providerName ->
                OperatorBasedVmWareOpenShiftStartDeployClusterTask.NAME
            else -> {
                throw NotSupportedException("Provided operator provider name `$providerName` is not supported. Choose one of ${
                    OperatorProviderName.values().joinToString()
                }")
            }
        })
    }

    @TaskAction
    fun launch() {
        project.logger.lifecycle("Operator based Deploy Cluster is about to start.")
    }
}
