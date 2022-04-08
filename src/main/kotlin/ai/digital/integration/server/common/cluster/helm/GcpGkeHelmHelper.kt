package ai.digital.integration.server.common.cluster.helm

import ai.digital.integration.server.common.cluster.setup.AwsEksHelper
import ai.digital.integration.server.common.cluster.setup.GcpGkeHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.InfrastructureInfo
import ai.digital.integration.server.common.domain.providers.GcpGkeProvider
import ai.digital.integration.server.common.domain.providers.Provider
import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.common.util.YamlFileUtil
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.gradle.api.Project
import java.io.File

open class GcpGkeHelmHelper(project: Project, productName: ProductName) : HelmHelper(project, productName) {

    private val gcpGkeHelper: GcpGkeHelper = GcpGkeHelper(project, productName, getProfile())

    fun launchCluster() {
        gcpGkeHelper.launchCluster()
    }

    fun setupHelmValues() {

    }

    fun shutdownCluster() {

    }

    override fun getProvider(): GcpGkeProvider {
        return getProfile().gcpGke
    }

    override fun updateCustomHelmValues(valuesFile: File) {
        /*  val pairs: MutableMap<String, Any> = mutableMapOf(
              "spec.ingress.hosts" to arrayOf(awsEksHelper.getFqdn()),
                "spec.rabbitmq.persistence.storageClass" to "gp2"
        )
        updateYamlFile(valuesFile, pairs)*/
    }
}
