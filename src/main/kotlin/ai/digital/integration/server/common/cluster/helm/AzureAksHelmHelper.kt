package ai.digital.integration.server.common.cluster.helm

import ai.digital.integration.server.common.cluster.setup.AzureAksHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.providers.AzureAksProvider
import ai.digital.integration.server.common.util.YamlFileUtil
import org.gradle.api.Project
import java.io.File

open class AzureAksHelmHelper(project: Project, productName: ProductName) : HelmHelper(project, productName) {

    private val azureAksHelper: AzureAksHelper = AzureAksHelper(project, productName, getProfile())

    fun launchCluster() {
        azureAksHelper.launchCluster()
    }

    fun setupHelmValues() {
        copyValuesYamlFile()
        updateHelmValuesYaml()
        updateHelmDependency()
    }

    fun helmInstallCluster() {
        installCluster()

        waitForDeployment(getProfile().ingressType.get(), getProfile().deploymentTimeoutSeconds.get(), skipOperator = true)
        waitForMasterPods(getProfile().deploymentTimeoutSeconds.get())
        waitForWorkerPods(getProfile().deploymentTimeoutSeconds.get())
        createClusterMetadata()
        waitForBoot(getContextRoot(), getFqdn())
    }

    fun shutdownCluster() {
        val azureAksProvider: AzureAksProvider = getProvider()
        val name = azureAksProvider.name.get()

        val groupName = azureAksHelper.resourceGroupName(name)
        val location = azureAksProvider.location.get()

        val existsResourceGroup = azureAksHelper.existsResourceGroup(groupName, location)
        if (existsResourceGroup) {
            helmCleanUpCluster()
            getKubectlHelper().deleteAllPVCs()
        }

        azureAksHelper.destroyClusterOnShutdown(existsResourceGroup, name, groupName, location)
    }

    override fun getProvider(): AzureAksProvider {
        return getProfile().azureAks
    }

    override fun getStorageClass(): String {
        return azureAksHelper.getStorageClass()
    }

    override fun getDbStorageClass(): String {
        return azureAksHelper.getDbStorageClass()
    }

    override fun updateCustomHelmValues(valuesFile: File) {
        val pairs: MutableMap<String, Any> = mutableMapOf(
                "nginx-ingress-controller.service.annotations" to mapOf("service.beta.kubernetes.io/azure-dns-label-name" to getHost()),
                "haproxy-ingress.controller.service.annotations" to mapOf("service.beta.kubernetes.io/azure-dns-label-name" to getHost()),
                "ingress.hosts" to arrayOf(getFqdn())
        )
        YamlFileUtil.overlayFile(valuesFile, pairs, minimizeQuotes = false)
    }

    override fun getFqdn(): String {
        return azureAksHelper.getFqdn()
    }
}
