package ai.digital.integration.server.deploy.internals.cluster.operator

import ai.digital.integration.server.common.domain.providers.operator.Provider
import ai.digital.integration.server.common.util.YamlFileUtil
import org.gradle.api.Project
import java.io.File

@Suppress("UnstableApiUsage")
open class AwsOpenshiftHelper(project: Project) : OperatorHelper(project) {

    private fun updateCrFile() {
        val file = File(getProviderHomeDir(), CR_REL_PATH)
        val pairs = mutableMapOf<String, Any>(
            "spec.postgresql.postgresqlExtendedConf.listenAddresses" to "*"
        )
        YamlFileUtil.overlayFile(file, pairs)
    }

    fun launchCluster() {
        updateCrFile()
        updateControllerManager()
        updateOperatorApplications()
        updateOperatorDeployment()
        updateOperatorDeploymentCr()
    }

    fun shutdownCluster() {

    }

    override fun getProviderHomeDir(): String {
        return "${getOperatorHomeDir()}/deploy-operator-openshift"
    }

    override fun getProvider(): Provider {
        return getProfile().awsOpenshift
    }

    override fun getOperatorImage(): String {
        return getProvider().operatorImage.value("xebialabs/deploy-operator:1.2.0-openshift").get()
    }
}
