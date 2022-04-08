package ai.digital.integration.server.common.cluster.helm


import ai.digital.integration.server.common.cluster.setup.AwsEksHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.providers.Provider
import org.gradle.api.Project
import java.io.File

open class VmwareOpenshiftHelmHelper(project: Project, productName: ProductName) : HelmHelper(project, productName) {

    fun setupHelmValues() {

    }

    fun shutdownCluster() {

    }

    override fun getProvider(): Provider {
        return getProfile().vmwareOpenshift
    }

    override fun updateCustomHelmValues(valuesFile: File) {
        /* val pairs: MutableMap<String, Any> = mutableMapOf(
                 "spec.ingress.hosts" to arrayOf(awsEksHelper.getFqdn()),
                 "spec.rabbitmq.persistence.storageClass" to "gp2"
         )
         updateYamlFile(valuesFile, pairs)*/
    }
}
