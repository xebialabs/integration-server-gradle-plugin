package ai.digital.integration.server.deploy.internals.cluster.operator

import ai.digital.integration.server.common.domain.InfrastructureInfo
import ai.digital.integration.server.common.domain.providers.operator.Provider
import org.gradle.api.Project

open class AzureAksHelper(project: Project) : OperatorHelper(project) {
    fun launchCluster() {
    }

    fun shutdownCluster() {
    }

    override fun updateInfrastructure(infraInfo: InfrastructureInfo) {

    }

    override fun getProviderHomeDir(): String {
        return "${getOperatorHomeDir()}/deploy-operator-azure-eks"
    }

    override fun getProvider(): Provider {
        return getProfile().azureAks
    }
}
