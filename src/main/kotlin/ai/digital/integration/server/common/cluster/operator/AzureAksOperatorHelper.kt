package ai.digital.integration.server.common.cluster.operator

import ai.digital.integration.server.common.cluster.setup.AzureAksHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.InfrastructureInfo
import ai.digital.integration.server.common.domain.providers.AzureAksProvider
import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.common.util.YamlFileUtil
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.gradle.api.Project
import java.io.File

open class AzureAksOperatorHelper(project: Project, productName: ProductName) : OperatorHelper(project, productName) {

    private val azureAksHelper: AzureAksHelper = AzureAksHelper(project, productName, getProfile())

    fun launchCluster(){
        azureAksHelper.launchCluster()
    }

    fun updateOperator() {
        cleanUpCluster(getProvider().cleanUpWaitTimeout.get())
        val kubeContextInfo = getCurrentContextInfo()
        updateInfrastructure(kubeContextInfo)
        updateOperatorApplications()
        updateOperatorDeployment()
        updateOperatorDeploymentCr()
        updateOperatorEnvironment()
        updateDeploymentValues()
        updateOperatorCrValues()
    }

    fun installCluster() {
        applyYamlFiles()
        turnOnLogging()
        val namespaceAsPrefix = getNamespace()?.let { "$it-" } ?: ""
        waitForDeployment(getProfile().ingressType.get(), getProfile().deploymentTimeoutSeconds.get(), namespaceAsPrefix)
        waitForMasterPods(getProfile().deploymentTimeoutSeconds.get())
        waitForWorkerPods(getProfile().deploymentTimeoutSeconds.get())

        createClusterMetadata()
        waitForBoot(getContextRoot(), getFqdn())
        turnOffLogging()
    }

    fun shutdownCluster() {
        val azureAksProvider: AzureAksProvider = getProvider()
        val name = azureAksProvider.name.get()

        val groupName = azureAksHelper.resourceGroupName(name)
        val location = azureAksProvider.location.get()

        val existsResourceGroup = azureAksHelper.existsResourceGroup(groupName, location)
        if (existsResourceGroup) {
            undeployCluster()
        }

        azureAksHelper.destroyClusterOnShutdown(existsResourceGroup, name, groupName, location)
    }

    private fun updateInfrastructure(infraInfo: InfrastructureInfo) {
        super.updateInfrastructure()

        val file = File(getProviderHomeDir(), OPERATOR_INFRASTRUCTURE_PATH)
        val pairs = mutableMapOf<String, Any>(
                "spec[0].children[0].apiServerURL" to infraInfo.apiServerURL!!,
                "spec[0].children[0].caCert" to infraInfo.caCert!!,
                "spec[0].children[0].tlsCert" to infraInfo.tlsCert!!,
                "spec[0].children[0].tlsPrivateKey" to infraInfo.tlsPrivateKey!!
        )
        YamlFileUtil.overlayFile(file, pairs)
    }

    override fun getProviderHomePath(): String {
        return "${getName()}-operator-azure-aks"
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

    override fun getFqdn(): String {
        val azureAksProvider: AzureAksProvider = getProvider()
        val location = azureAksProvider.location.get()
        return "${getHost()}.${location}.cloudapp.azure.com"
    }

    override fun updateCustomOperatorCrValues(crValuesFile: File) {
        val pairs: MutableMap<String, Any> = mutableMapOf(
            "spec.nginx-ingress-controller.service.annotations" to mapOf("service.beta.kubernetes.io/azure-dns-label-name" to getHost()),
            "spec.haproxy-ingress.controller.service.annotations" to mapOf("service.beta.kubernetes.io/azure-dns-label-name" to getHost()),
            "spec.ingress.hosts" to arrayOf(getFqdn())
        )
        YamlFileUtil.overlayFile(crValuesFile, pairs, minimizeQuotes = false)
    }

    override fun getCurrentContextInfo() = getKubectlHelper().getCurrentContextInfo()

    fun getAccessToken(): String {
        val azToken = ProcessUtil.executeCommand(project,
                "az account get-access-token -o yaml")
        val azConfigMap = ObjectMapper(YAMLFactory.builder().build()).readValue(azToken, MutableMap::class.java)
        return azConfigMap["accessToken"] as String
    }
}
