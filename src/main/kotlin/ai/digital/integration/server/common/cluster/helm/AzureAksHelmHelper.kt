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

open class AzureAksHelmHelper(project: Project, productName: ProductName) : HelmHelper(project, productName)
{
    fun updateHelmValues() {

    }

    fun installCluster() {

    }

    fun shutdownCluster() {

    }

    override fun getProvider(): AzureAksProvider {
        return getProfile().azureAks
    }
}
