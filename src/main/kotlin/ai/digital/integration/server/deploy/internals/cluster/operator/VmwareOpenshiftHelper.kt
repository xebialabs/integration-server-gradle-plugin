package ai.digital.integration.server.deploy.internals.cluster.operator

import ai.digital.integration.server.common.domain.providers.operator.Provider
import org.gradle.api.Project

open class VmwareOpenshiftHelper(project: Project): OperatorHelper(project) {

    fun launchCluster() {

    }

    fun shutdownCluster() {

    }

    override fun getProviderHomeDir(): String {
        return "${getOperatorHomeDir()}/deploy-operator-vmware-openshift"
    }

    override fun getProvider(): Provider {
        return getProfile().vmwareOpenshift
    }

}
