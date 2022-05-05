package ai.digital.integration.server.common.cluster.helm

import ai.digital.integration.server.common.cluster.setup.AwsOpenshiftHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.providers.AwsOpenshiftProvider
import ai.digital.integration.server.common.util.KubeCtlHelper
import ai.digital.integration.server.common.util.YamlFileUtil
import org.gradle.api.Project
import java.io.File

open class AwsOpenshiftHelmHelper(project: Project, productName: ProductName) : HelmHelper(project, productName) {

    private val awsOpenshiftHelper: AwsOpenshiftHelper = AwsOpenshiftHelper(project, productName, getProfile())

    fun launchCluster() {
        awsOpenshiftHelper.launchCluster()
    }

    fun setupHelmValues() {
        updateHelmValuesYaml()
        updateHelmDependency()
    }

    fun helmInstallCluster() {
        installCluster()

        awsOpenshiftHelper.ocLogin()
        waitForDeployment(getProfile().ingressType.get(), getProfile().deploymentTimeoutSeconds.get(), skipOperator = true)
        waitForMasterPods(getProfile().deploymentTimeoutSeconds.get())
        waitForWorkerPods(getProfile().deploymentTimeoutSeconds.get())

        createClusterMetadata()
        waitForBoot(getContextRoot(), getFqdn())
    }

    fun shutdownCluster() {
        awsOpenshiftHelper.ocLogin()
        helmCleanUpCluster()
        getKubectlHelper().deleteAllPVCs()
        awsOpenshiftHelper.ocLogout()
    }

    override fun getProvider(): AwsOpenshiftProvider {
        return getProfile().awsOpenshift
    }

    override fun updateCustomHelmValues(valuesFile: File) {
        val pairs: MutableMap<String, Any> =
                mutableMapOf("postgresql.postgresqlExtendedConf.listenAddresses" to "*")
        YamlFileUtil.overlayFile(valuesFile, pairs, minimizeQuotes = false)
    }

    override fun getStorageClass(): String {
        return awsOpenshiftHelper.getStorageClass()
    }

    override fun getKubectlHelper(): KubeCtlHelper = KubeCtlHelper(project, null, true)

    override fun hasIngress(): Boolean = false

    override fun getFqdn(): String {
        return awsOpenshiftHelper.getFqdn()
    }

    override fun getWorkerPodName(position: Int) =
            "pod/${getCrName()}-digitalai-${getName()}-ocp-worker-$position"

    override fun getMasterPodName(position: Int) =
            "pod/${getCrName()}-digitalai-${getName()}-ocp-${getMasterPodNameSuffix(position)}"

    override fun getContextPath(): String = "route.path"

}
