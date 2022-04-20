package ai.digital.integration.server.common.cluster.helm

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
    }
}
