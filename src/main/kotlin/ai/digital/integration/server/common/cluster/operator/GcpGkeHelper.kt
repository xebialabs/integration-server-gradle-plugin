package ai.digital.integration.server.common.cluster.operator

import ai.digital.integration.server.common.cluster.setup.GcpGke
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.InfrastructureInfo
import ai.digital.integration.server.common.domain.providers.GcpGkeProvider
import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.common.util.YamlFileUtil
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.gradle.api.Project
import java.io.File

open class GcpGkeHelper(project: Project, productName: ProductName) : OperatorHelper(project, productName) {

    fun updateOperator() {
        cleanUpCluster(getProvider().cleanUpWaitTimeout.get())
        val gcpGkeProvider: GcpGkeProvider = getProvider()
        val projectName = gcpGkeProvider.projectName.get()
        val accountName = gcpGkeProvider.accountName.get()
        val kubeContextInfo = getCurrentContextInfo(accountName, projectName)
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
        waitForDeployment()
        waitForMasterPods()
        waitForWorkerPods()
        val ip = getKubectlHelper().getServiceExternalIp("service/dai-${getPrefixName()}-nginx-ingress-controller")
        val nameSpace = getNamespace() ?: "default"
        GcpGke(project, productName).applyDnsOpenApi(ip, getFqdn(), getHost(), nameSpace)
        createClusterMetadata()
        waitForBoot()
    }

    fun shutdownCluster() {
        val gcpGkeProvider: GcpGkeProvider = getProvider()
        val projectName = gcpGkeProvider.projectName.get()
        val name = gcpGkeProvider.name.get()
        val regionZone = gcpGkeProvider.regionZone.get()
        val accountName = gcpGkeProvider.accountName.get()

        val existsCluster = GcpGke(project, productName).existsCluster(accountName, projectName, name, regionZone)
        if (existsCluster) {
            undeployCluster()
        }
        GcpGke(project, productName).destroyClusterOnShutdown(existsCluster, accountName, projectName, name, regionZone, getFqdn())
    }

    override fun getProviderHomePath(): String {
        return "${getName()}-operator-gcp-gke"
    }

    override fun getProvider(): GcpGkeProvider {
        return GcpGke(project, productName).getProvider()
    }

    override fun getFqdn(): String {
        return "${productName.shortName}-${getHost()}-${getNamespace() ?: "default"}.endpoints.${getProvider().projectName.get()}.cloud.goog"
    }

    private fun getCurrentContextInfo(accountName: String, projectName: String): Pair<InfrastructureInfo, String> {
        val kubectlHelper = getKubectlHelper()
        val context = kubectlHelper.getCurrentContext()
        val cluster = kubectlHelper.getContextCluster(context)
        val user = kubectlHelper.getContextUser(context)

        val info = InfrastructureInfo(
                clusterName = cluster,
                userName = user,
                apiServerURL = kubectlHelper.getClusterServer(cluster),
                caCert = kubectlHelper.getClusterCertificateAuthorityData(cluster),
                tlsCert = null,
                tlsPrivateKey = null
        )
        val accessToken = getAccessToken(accountName, projectName)

        return Pair(info, accessToken)
    }

    private fun getAccessToken(accountName: String, projectName: String): String {
        val gcloudConfig = ProcessUtil.executeCommand(project,
                "gcloud config --account \"$accountName\" --project \"$projectName\" config-helper --format=yaml")
        val gcloudConfigMap = ObjectMapper(YAMLFactory.builder().build()).readValue(gcloudConfig, MutableMap::class.java)
        return (gcloudConfigMap["credential"] as Map<*, *>)["access_token"] as String
    }

    private fun updateInfrastructure(kubeContextInfo: Pair<InfrastructureInfo, String>) {
        val file = File(getProviderHomeDir(), OPERATOR_INFRASTRUCTURE_PATH)
        val pairs = mutableMapOf<String, Any>(
                "spec[0].children[0].apiServerURL" to kubeContextInfo.first.apiServerURL!!,
                "spec[0].children[0].caCert" to kubeContextInfo.first.caCert!!,
                "spec[0].children[0].token" to kubeContextInfo.second
        )
        YamlFileUtil.overlayFile(file, pairs)
    }

    override fun updateCustomOperatorCrValues(crValuesFile: File) {
        val pairs: MutableMap<String, Any> = mutableMapOf(
                "spec.ingress.hosts" to listOf(getFqdn())
        )
        YamlFileUtil.overlayFile(crValuesFile, pairs, minimizeQuotes = false)
    }

    override fun getCurrentContextInfo(): InfrastructureInfo {
        val projectName = getProvider().projectName.get()
        val accountName = getProvider().accountName.get()
        return getCurrentContextInfo(accountName, projectName).first
    }

    fun getAccessToken(): String {
        val projectName = getProvider().projectName.get()
        val accountName = getProvider().accountName.get()
        return getAccessToken(accountName, projectName)
    }
}
