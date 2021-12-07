package ai.digital.integration.server.deploy.internals.cluster.operator

import ai.digital.integration.server.common.domain.providers.operator.Provider
import org.gradle.api.Project

open class OnPremHelper(project: Project): OperatorHelper(project) {

    fun launchCluster() {

    }

    fun shutdownCluster() {

    }

    override fun getProviderHomeDir(): String {
        return "${getOperatorHomeDir()}/deploy-operator-on-premise"
    }

    override fun getProvider(): Provider {
        return getProfile().onPremise
    }

}
