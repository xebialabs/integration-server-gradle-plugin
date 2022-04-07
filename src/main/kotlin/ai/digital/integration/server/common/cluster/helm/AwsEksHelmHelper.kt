package ai.digital.integration.server.common.cluster.helm

import ai.digital.integration.server.common.cluster.setup.AwsEksHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.providers.AwsEksProvider
import ai.digital.integration.server.common.domain.providers.Provider
import ai.digital.integration.server.common.util.YamlFileUtil
import org.gradle.api.Project
import java.io.File

open class AwsEksHelmHelper(project: Project, productName: ProductName) : HelmHelper(project, productName) {

    private val awsEksHelper: AwsEksHelper = AwsEksHelper(project, ProductName.DEPLOY, getProfile())

    fun launchCluster() {
        awsEksHelper.launchCluster()
    }

    fun setupHelmValues() {
        copyValuesFile()
        updateHelmValues()
    }

    fun installCluster() {

    }

    fun shutdownCluster() {

    }

    override fun getProvider(): AwsEksProvider {
        return getProfile().awsEks
    }

    override fun getStorageClass(): String {
        return awsEksHelper.getStorageClass()
    }

    override fun getDbStorageClass(): String {
        return ("gp2")
    }

    override fun getMqStorageClass(): String {
        return ("gp2")
    }

    override fun updateCustomHelmValues(valuesFile: File) {
        val pairs: MutableMap<String, Any> = mutableMapOf(
                ".ingress.hosts[0]" to awsEksHelper.getFqdn(),
                ".rabbitmq.persistence.storageClass" to "gp2"
        )
        updateYamlFile(valuesFile, pairs)
    }
}
