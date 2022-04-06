package ai.digital.integration.server.common.cluster.operator

import ai.digital.integration.server.common.cluster.setup.AwsEks
import ai.digital.integration.server.common.cluster.setup.OnPrem
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.InfrastructureInfo
import ai.digital.integration.server.common.domain.providers.OnPremiseProvider
import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.common.util.YamlFileUtil
import org.gradle.api.Project
import org.gradle.api.provider.Property
import java.io.File

open class OnPremHelper(project: Project, productName: ProductName) : OperatorHelper(project, productName) {

    fun updateOperator() {
        OnPrem(project, productName).updateEtcHosts(getProvider().name.get() , getFqdn())
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
        waitForDeployment()
        waitForMasterPods()
        waitForWorkerPods()

        createClusterMetadata()
        waitForBoot()
    }

    fun shutdownCluster() {
        undeployCluster()
        OnPrem(project, productName).destroyClusterOnShutdown()
    }

    override fun getProviderHomePath(): String {
        return "${getName()}-operator-onprem"
    }

    override fun getProvider(): OnPremiseProvider {
        return OnPrem(project, productName).getProvider()
    }

     fun updateInfrastructure(infraInfo: InfrastructureInfo) {
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
            "spec.ingress.hosts" to listOf(getFqdn()),
            "spec.nginx-ingress-controller.defaultBackend.podSecurityContext" to mapOf("fsGroup" to 1001),
            "spec.nginx-ingress-controller.podSecurityContext" to mapOf("fsGroup" to 1001)
        )
        YamlFileUtil.overlayFile(crValuesFile, pairs, minimizeQuotes = false)
    }

    override fun getCurrentContextInfo() = getKubectlHelper().getCurrentContextInfo()
}
