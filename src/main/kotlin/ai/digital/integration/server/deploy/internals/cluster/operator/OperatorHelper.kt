package ai.digital.integration.server.deploy.internals.cluster.operator

import ai.digital.integration.server.common.domain.profiles.OperatorProfile
import ai.digital.integration.server.common.domain.providers.operator.Provider
import ai.digital.integration.server.common.util.YamlFileUtil
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import ai.digital.integration.server.deploy.internals.KubeCtlUtil
import org.gradle.api.Project
import java.io.File

const val OPERATOR_FOLDER_NAME: String = "xl-deploy-kubernetes-operator"

const val CR_REL_PATH = "digitalai-deploy/kubernetes/daideploy_cr.yaml"

const val CONTROLLER_MANAGER_REL_PATH = "digitalai-deploy/kubernetes/template/deployment.yaml"

const val OPERATOR_PACKAGE_REL_PATH = "digitalai-deploy/deployment.yaml"

const val OPERATOR_CR_PACKAGE_REL_PATH = "digitalai-deploy/deployment-cr.yaml"

const val OPERATOR_INFRASTRUCTURE_PATH = "digitalai-deploy/infrastructure.yaml"

abstract class OperatorHelper(val project: Project) {
    fun getOperatorHomeDir(): String =
        project.buildDir.toPath().resolve(OPERATOR_FOLDER_NAME).toAbsolutePath().toString()

    fun getProfile(): OperatorProfile {
        return DeployExtensionUtil.getExtension(project).clusterProfiles.operator()
    }

    fun updateControllerManager() {
        val file = File(getProviderHomeDir(), CONTROLLER_MANAGER_REL_PATH)
        val pairs = mutableMapOf<String, Any>(
            "spec.template.spec.containers[1].image" to getProvider().operatorImage
        )
        YamlFileUtil.overlayFile(file, pairs)
    }

    fun updateOperatorDeployment() {
        val file = File(getProviderHomeDir(), OPERATOR_PACKAGE_REL_PATH)
        val pairs = mutableMapOf<String, Any>(
            "spec.package" to "Applications/xld-operator-app/${getProvider().operatorPackageVersion}"
        )
        YamlFileUtil.overlayFile(file, pairs)
    }

    fun updateOperatorDeploymentCr() {
        val file = File(getProviderHomeDir(), OPERATOR_CR_PACKAGE_REL_PATH)
        val pairs = mutableMapOf<String, Any>(
            "spec.package" to "Applications/xld-cr/${getProvider().operatorPackageVersion}"
        )
        YamlFileUtil.overlayFile(file, pairs)
    }

    fun updateInfrastructure(kubeContextInfo: KubeCtlUtil.Companion.KubeContextInfo) {
        val file = File(getProviderHomeDir(), OPERATOR_INFRASTRUCTURE_PATH)
        val pairs = mutableMapOf<String, Any>(
            "spec[0].children[0].apiServerURL" to kubeContextInfo.clusterServer,
            "spec[0].children[0].caCert" to kubeContextInfo.clusterCertificateAuthorityData,
            "spec[0].children[0].tlsCert" to kubeContextInfo.clientCertificateData,
            "spec[0].children[0].tlsPrivateKey" to kubeContextInfo.clientKeyData
        )
        YamlFileUtil.overlayFile(file, pairs)
    }

    abstract fun getProviderHomeDir(): String

    abstract fun getProvider(): Provider
}
