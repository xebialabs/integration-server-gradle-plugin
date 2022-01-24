package ai.digital.integration.server.common.cluster.operator


import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.providers.operator.Provider
import org.gradle.api.Project

open class VmwareOpenshiftHelper(project: Project, productName: ProductName) : OperatorHelper(project, productName) {

    fun launchCluster() {

    }

    fun shutdownCluster() {

    }

    override fun getProviderHomePath(): String {
        return "${getName()}-operator-vmware-openshift"
    }

    override fun getProvider(): Provider {
        return getProfile().vmwareOpenshift
    }

}
