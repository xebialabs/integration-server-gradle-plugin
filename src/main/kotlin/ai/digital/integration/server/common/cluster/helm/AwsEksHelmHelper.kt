package ai.digital.integration.server.common.cluster.helm

import ai.digital.integration.server.common.cluster.setup.AwsEksHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.providers.Provider
import org.gradle.api.Project

open class AwsEksHelmHelper(project: Project, productName: ProductName) : HelmHelper(project, productName) {

    fun updateHelmValues() {

    }

    fun installCluster() {

    }

    fun shutdownCluster() {

    }

    override fun getProvider(): Provider {
        return AwsEksHelper(project,productName).getProvider()
    }
}
