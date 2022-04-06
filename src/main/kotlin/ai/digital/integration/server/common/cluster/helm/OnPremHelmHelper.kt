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
