package ai.digital.integration.server.common.cluster.helm

import ai.digital.integration.server.common.cluster.setup.AwsEksHelper
import ai.digital.integration.server.common.cluster.setup.OnPremHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.InfrastructureInfo
import ai.digital.integration.server.common.domain.providers.OnPremiseProvider
import ai.digital.integration.server.common.domain.providers.Provider
import ai.digital.integration.server.common.util.YamlFileUtil
import org.gradle.api.Project
import java.io.File

open class OnPremHelmHelper(project: Project, productName: ProductName) : HelmHelper(project, productName) {

    private val onPremHelper: OnPremHelper = OnPremHelper(project, productName, getProfile())

    fun launchCluster() {
        onPremHelper.launchCluster()
    }

    fun setupHelmValues() {

    }


    fun shutdownCluster() {

    }

    override fun getProvider(): OnPremiseProvider {
        return getProfile().onPremise
    }

    override fun updateCustomHelmValues(valuesFile: File) {
       /* val pairs: MutableMap<String, Any> = mutableMapOf(
                "spec.ingress.hosts" to arrayOf(awsEksHelper.getFqdn()),
                "spec.rabbitmq.persistence.storageClass" to "gp2"
        )
        updateYamlFile(valuesFile, pairs)*/
    }
}
