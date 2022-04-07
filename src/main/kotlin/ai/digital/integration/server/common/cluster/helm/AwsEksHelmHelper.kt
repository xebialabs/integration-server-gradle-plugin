package ai.digital.integration.server.common.cluster.helm

import ai.digital.integration.server.common.cluster.setup.AwsEksHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.providers.AwsEksProvider
import ai.digital.integration.server.common.domain.providers.Provider
import org.gradle.api.Project

open class AwsEksHelmHelper(project: Project, productName: ProductName) : HelmHelper(project, productName) {

    fun launchCluster() {
        AwsEksHelper(project, ProductName.DEPLOY,getProfile()).launchCluster()
    }

    fun updateHelmValues() {

    }

    fun installCluster() {

    }

    fun shutdownCluster() {

    }

    override fun getProvider(): AwsEksProvider {
        return getProfile().awsEks
    }
}
