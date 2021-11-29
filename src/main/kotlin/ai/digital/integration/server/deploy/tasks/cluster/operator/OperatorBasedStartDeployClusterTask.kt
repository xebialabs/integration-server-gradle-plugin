package ai.digital.integration.server.deploy.tasks.cluster.operator

import ai.digital.integration.server.common.constant.OperatorProviderName
import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import ai.digital.integration.server.deploy.tasks.cluster.operator.awseks.OperatorBasedAwsEksDeployClusterStartTask
import ai.digital.integration.server.deploy.tasks.cluster.operator.awsopenshift.OperatorBasedAwsOpenShiftDeployClusterStartTask
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

        this.dependsOn(when (val providerName = DeployClusterUtil.getOperatorProvider(project)) {
            OperatorProviderName.AWS_EKS.providerName ->
                OperatorBasedAwsEksDeployClusterStartTask.NAME
            OperatorProviderName.AWS_OPENSHIFT.providerName ->
                OperatorBasedAwsOpenShiftDeployClusterStartTask.NAME
            OperatorProviderName.AZURE_AKS.providerName ->
                OperatorBasedAzureAksStartDeployClusterTask.NAME
            OperatorProviderName.GCP_GKE.providerName ->
                OperatorBasedGcpGkeStartDeployClusterTask.NAME
            OperatorProviderName.ON_PREMISE.providerName ->
                OperatorBasedOnPremStartDeployClusterTask.NAME
            OperatorProviderName.VMWARE_OPENSHIFT.providerName ->
                OperatorBasedVmWareOpenShiftStartDeployClusterTask.NAME
            else -> {
                throw IllegalArgumentException("Provided operator provider name `$providerName` is not supported. Choose one of ${
                    OperatorProviderName.values().joinToString()
                }")
            }
        })
    }

    @TaskAction
    fun launch() {
        val providerName = DeployClusterUtil.getOperatorProvider(project)
        project.logger.lifecycle("Operator based Deploy Cluster with provider $providerName is about to start.")
        cloneRepository()
    }

    private fun cloneRepository() {
        val buildDirPath = project.buildDir.toPath().toAbsolutePath().toString()
        val dest = "$buildDirPath/xl-deploy-kubernetes-operator"
        ProcessUtil.executeCommand(project,
            "git clone git@github.com:xebialabs/xl-deploy-kubernetes-operator.git $dest")
    }
}
