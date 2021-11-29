package ai.digital.integration.server.deploy.internals.cluster.operator

import ai.digital.integration.server.common.domain.providers.operator.AwsOpenshiftProvider
import ai.digital.integration.server.common.util.YamlFileUtil
import org.gradle.api.Project
import java.io.File

open class AwsOpenshiftHelper(project: Project) : OperatorHelper(project) {

    private fun getOpenshiftOperatorHomeDir(): String {
        return "${getOperatorHomeDir()}/deploy-operator-openshift"
    }

    private fun getAwsOpenshiftProfile(): AwsOpenshiftProvider {
        return getProfile().awsOpenshift
    }

    private fun updateCrFile() {
        val file = File(getOpenshiftOperatorHomeDir(), CR_REL_PATH)
        val pairs = mutableMapOf<String, Any>(
            "spec.postgresql.postgresqlExtendedConf.listenAddresses" to "*"
        )
        YamlFileUtil.overlayFile(file, pairs)
    }

    private fun updateDeployment() {
        val file = File(getOpenshiftOperatorHomeDir(), DEPLOYMENT_REL_PATH)
        val pairs = mutableMapOf<String, Any>(
            "spec.template.spec.containers[1].image" to getAwsOpenshiftProfile().operatorImage
        )
        YamlFileUtil.overlayFile(file, pairs)
    }

    fun launchCluster() {
        updateCrFile()
        updateDeployment()
    }

    fun shutdownCluster() {

    }

}
