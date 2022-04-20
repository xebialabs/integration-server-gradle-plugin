package ai.digital.integration.server.common.cluster.helm

import ai.digital.integration.server.common.cluster.setup.OnPremHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.providers.OnPremiseProvider
import ai.digital.integration.server.common.util.YamlFileUtil
import org.gradle.api.Project
import java.io.File

open class OnPremHelmHelper(project: Project, productName: ProductName) : HelmHelper(project, productName) {

    private val onPremHelper: OnPremHelper = OnPremHelper(project, productName, getProfile())

    fun launchCluster() {
        onPremHelper.launchCluster()
    }

    fun setupHelmValues() {
        onPremHelper.updateEtcHosts(getProvider().name.get() , getFqdn())
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
        helmCleanUpCluster()
        getKubectlHelper().deleteAllPVCs()
        onPremHelper.destroyClusterOnShutdown()
    }

    override fun getProvider(): OnPremiseProvider {
        return getProfile().onPremise
    }

    override fun getFqdn(): String {
        return "${getHost()}.digitalai-testing.com"
    }

    override fun updateCustomHelmValues(valuesFile: File) {
        val pairs: MutableMap<String, Any> = mutableMapOf(
                "ingress.hosts" to arrayOf(getFqdn()),
                "nginx-ingress-controller.defaultBackend.podSecurityContext" to mapOf("fsGroup" to 1001),
                "nginx-ingress-controller.podSecurityContext" to mapOf("fsGroup" to 1001)
        )
        YamlFileUtil.overlayFile(valuesFile, pairs, minimizeQuotes = false)
    }


}
