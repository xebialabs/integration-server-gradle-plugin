package ai.digital.integration.server.common.cluster.operator

import ai.digital.integration.server.common.cluster.setup.AwsEks
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.providers.AwsEksProvider
import ai.digital.integration.server.common.util.YamlFileUtil
import org.gradle.api.Project
import java.io.File

open class AwsEksHelper(project: Project, productName: ProductName) : OperatorHelper(project, productName) {

    fun updateOperator() {
        cleanUpCluster(getProvider().cleanUpWaitTimeout.get())
        updateInfrastructure()
        updateOperatorApplications()
        updateOperatorDeployment()
        updateOperatorDeploymentCr()
        updateDeploymentValues()
        updateOperatorCrValues()
    }

    fun installCluster() {
        applyYamlFiles()
        waitForDeployment()
        waitForMasterPods()
        waitForWorkerPods()

        createClusterMetadata()
        AwsEks(project, productName).updateRoute53()
        waitForBoot()
    }

    fun shutdownCluster() {
        undeployCluster()
        AwsEks(project,productName).destroyClusterOnShutdown()
    }

    override fun updateCustomOperatorCrValues(crValuesFile: File) {
        val pairs: MutableMap<String, Any> = mutableMapOf(
            "spec.ingress.hosts" to arrayOf(getFqdn()),
            "spec.rabbitmq.persistence.storageClass" to "gp2"
        )
        YamlFileUtil.overlayFile(crValuesFile, pairs, minimizeQuotes = false)
    }

    override fun getProviderHomePath(): String {
        return "${getName()}-operator-aws-eks"
    }

    override fun getProvider(): AwsEksProvider {
        return getProfile().awsEks
    }

    override fun getStorageClass(): String {
        return getProvider().storageClass.getOrElse("aws-efs")
    }

    private fun updateInfrastructure() {
        val infraInfo = getCurrentContextInfo()
        val file = File(getProviderHomeDir(), OPERATOR_INFRASTRUCTURE_PATH)
        val awsEksProvider: AwsEksProvider = getProvider()
        val pairs = mutableMapOf<String, Any>(
            "spec[0].children[0].apiServerURL" to infraInfo.apiServerURL!!,
            "spec[0].children[0].caCert" to infraInfo.caCert!!,
            "spec[0].children[0].accessKey" to awsEksProvider.getAwsAccessKey(),
            "spec[0].children[0].accessSecret" to awsEksProvider.getAwsSecretKey(),
            "spec[0].children[0].regionName" to awsEksProvider.region.get(),
            "spec[0].children[0].clusterName" to awsEksProvider.clusterName.get()
        )
        YamlFileUtil.overlayFile(file, pairs)
    }

    override fun getFqdn(): String {
        return "${getProvider().stack.get()}-${getName()}.digitalai-testing.com"
    }

    override fun getDbStorageClass(): String {
        return ("gp2")
    }

    override fun getMqStorageClass(): String {
        return ("gp2")
    }

    override fun getCurrentContextInfo() = getKubectlHelper().getCurrentContextInfo(skip = true)

}
