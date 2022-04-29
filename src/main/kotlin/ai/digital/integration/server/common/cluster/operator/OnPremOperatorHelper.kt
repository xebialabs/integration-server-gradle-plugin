package ai.digital.integration.server.common.cluster.operator

import ai.digital.integration.server.common.cluster.setup.OnPremHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.InfrastructureInfo
import ai.digital.integration.server.common.domain.providers.OnPremiseProvider
import ai.digital.integration.server.common.util.YamlFileUtil
import org.gradle.api.Project
import java.io.File

open class OnPremOperatorHelper(project: Project, productName: ProductName) : OperatorHelper(project, productName) {

    private val onPremHelper : OnPremHelper = OnPremHelper(project, productName, getProfile())

    fun launchCluster(){
        onPremHelper.launchCluster()
    }

    fun updateOperator() {
        onPremHelper.updateEtcHosts(getProvider().name.get() , getFqdn())
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
        val namespaceAsPrefix = getNamespace()?.let { "$it-" } ?: ""
        waitForDeployment(getProfile().ingressType.get(), getProfile().deploymentTimeoutSeconds.get(), namespaceAsPrefix)
        waitForMasterPods(getProfile().deploymentTimeoutSeconds.get())
        waitForWorkerPods(getProfile().deploymentTimeoutSeconds.get())

        createClusterMetadata()
        waitForBoot(getContextRoot(), getFqdn())
    }

    fun shutdownCluster() {
        undeployCluster()
        onPremHelper.destroyClusterOnShutdown()
    }

    override fun getProviderHomePath(): String {
        return "${getName()}-operator-onprem"
    }

    override fun getProvider(): OnPremiseProvider {
        return getProfile().onPremise
    }

    fun updateInfrastructure(infraInfo: InfrastructureInfo) {
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

    override fun getFqdn(): String {
        return "${getHost()}.digitalai-testing.com"
    }

    override fun updateCustomOperatorCrValues(crValuesFile: File) {
        val pairs: MutableMap<String, Any> = mutableMapOf(
            "spec.ingress.hosts" to listOf(getFqdn())
        )
        YamlFileUtil.overlayFile(crValuesFile, pairs, minimizeQuotes = false)
    }

    override fun getCurrentContextInfo() = getKubectlHelper().getCurrentContextInfo()

    override fun hasIngress(): Boolean = false
}
