package ai.digital.integration.server.common.cluster.helm

import ai.digital.integration.server.common.cluster.setup.AwsEksHelper
import ai.digital.integration.server.common.cluster.setup.AzureAksHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.InfrastructureInfo
import ai.digital.integration.server.common.domain.providers.AzureAksProvider
import ai.digital.integration.server.common.domain.providers.Provider
import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.common.util.YamlFileUtil
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.gradle.api.Project
import java.io.File

open class AzureAksHelmHelper(project: Project, productName: ProductName) : HelmHelper(project, productName) {

    private val azureAksHelper: AzureAksHelper = AzureAksHelper(project, productName, getProfile())

    fun launchCluster() {
        azureAksHelper.launchCluster()
    }

    fun setupHelmValues() {

    }

    fun installCluster() {

    }

    fun shutdownCluster() {

    }

    override fun getProvider(): AzureAksProvider {
        return getProfile().azureAks
    }

    override fun updateCustomHelmValues(valuesFile: File) {
        /*val pairs: MutableMap<String, Any> = mutableMapOf(
                "spec.ingress.hosts" to arrayOf(awsEksHelper.getFqdn()),
                "spec.rabbitmq.persistence.storageClass" to "gp2"
        )
        updateYamlFile(valuesFile, pairs)*/
    }
}
