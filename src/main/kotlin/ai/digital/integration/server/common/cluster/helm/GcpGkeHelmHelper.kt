package ai.digital.integration.server.common.cluster.helm

import ai.digital.integration.server.common.cluster.setup.GcpGkeHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.providers.GcpGkeProvider
import ai.digital.integration.server.common.util.YamlFileUtil
import org.gradle.api.Project
import java.io.File

open class GcpGkeHelmHelper(project: Project, productName: ProductName) : HelmHelper(project, productName) {

    private val gcpGkeHelper: GcpGkeHelper = GcpGkeHelper(project, productName, getProfile())

    fun launchCluster() {
        gcpGkeHelper.launchCluster()
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
        val ip = getKubectlHelper().getServiceExternalIp("service/${getCrName()}-nginx-ingress-controller")
        gcpGkeHelper.applyDnsOpenApi(ip, getFqdn(), getHost())
        createClusterMetadata()
        waitForBoot(getContextRoot(), getFqdn())
    }

    fun shutdownCluster() {
        val gcpGkeProvider: GcpGkeProvider = getProvider()
        val projectName = gcpGkeProvider.projectName.get()
        val name = gcpGkeProvider.name.get()
        val regionZone = gcpGkeProvider.regionZone.get()
        val accountName = gcpGkeProvider.accountName.get()

        val existsCluster = gcpGkeHelper.existsCluster(accountName, projectName, name, regionZone)
        if (existsCluster) {
            helmCleanUpCluster()
            getKubectlHelper().deleteAllPVCs()
        }
        gcpGkeHelper.destroyClusterOnShutdown(existsCluster, accountName, projectName, name, regionZone, getFqdn())
    }

    override fun getProvider(): GcpGkeProvider {
        return getProfile().gcpGke
    }

    override fun updateCustomHelmValues(valuesFile: File) {
        val pairs: MutableMap<String, Any> = mutableMapOf(
                "ingress.hosts" to arrayOf(getFqdn())
        )
        YamlFileUtil.overlayFile(valuesFile, pairs, minimizeQuotes = false)
    }

    override fun getFqdn(): String {
        return gcpGkeHelper.getFqdn()
    }
}
