package ai.digital.integration.server.deploy.internals.cluster.operator

import ai.digital.integration.server.common.domain.InfrastructureInfo
import ai.digital.integration.server.common.domain.profiles.OperatorProfile
import ai.digital.integration.server.common.domain.providers.operator.Provider
import ai.digital.integration.server.common.util.YamlFileUtil
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import org.gradle.api.Project
import java.io.File
import java.nio.file.Files
import java.util.*

const val OPERATOR_FOLDER_NAME: String = "xl-deploy-kubernetes-operator"

const val CR_REL_PATH = "digitalai-deploy/kubernetes/daideploy_cr.yaml"

const val CONTROLLER_MANAGER_REL_PATH = "digitalai-deploy/kubernetes/template/deployment.yaml"

const val OPERATOR_APPS_REL_PATH = "digitalai-deploy/applications.yaml"

const val OPERATOR_INFRASTRUCTURE_PATH = "digitalai-deploy/infrastructure.yaml"

const val OPERATOR_CR_PACKAGE_REL_PATH = "digitalai-deploy/deployment-cr.yaml"

const val OPERATOR_CR_VALUES_REL_PATH = "digitalai-deploy/kubernetes/daideploy_cr.yaml"

const val OPERATOR_PACKAGE_REL_PATH = "digitalai-deploy/deployment.yaml"

@Suppress("UnstableApiUsage")
abstract class OperatorHelper(val project: Project) {
    fun getOperatorHomeDir(): String =
        project.buildDir.toPath().resolve(OPERATOR_FOLDER_NAME).toAbsolutePath().toString()

    fun getProfile(): OperatorProfile {
        return DeployExtensionUtil.getExtension(project).clusterProfiles.operator()
    }

    fun updateControllerManager() {
        val file = File(getProviderHomeDir(), CONTROLLER_MANAGER_REL_PATH)
        val pairs = mutableMapOf<String, Any>(
            "spec.template.spec.containers[1].image" to getOperatorImage()
        )
        YamlFileUtil.overlayFile(file, pairs)
    }

    fun updateOperatorApplications() {
        val file = File(getProviderHomeDir(), OPERATOR_APPS_REL_PATH)
        val pairs = mutableMapOf<String, Any>(
            "spec[0].children[0].name" to getProvider().operatorPackageVersion
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

    fun updateOperatorCrValues() {
        val file = File(getProviderHomeDir(), OPERATOR_CR_VALUES_REL_PATH)
        val pairs = mutableMapOf<String, Any>(
            "spec.ImageTag" to DeployServerUtil.getServer(project).version!!,
            "spec.KeystorePassphrase" to getProvider().keystorePassphrase,
            "spec.Persistence.StorageClass" to getStorageClass(),
            "spec.RepositoryKeystore" to getProvider().repositoryKeystore,
            "spec.postgresql.persistence.storageClass" to getStorageClass(),
            "spec.rabbitmq.persistence.storageClass" to getStorageClass(),
            "spec.rabbitmq.persistence.replicaCount" to "1",
            "spec.route.hosts" to arrayOf(getProvider().host),
            "spec.xldLicense" to getLicense()
        )
        YamlFileUtil.overlayFile(file, pairs)
    }

    private fun getLicense(): String {
        val deployitLicenseFile = File(DeployServerUtil.getConfDir(project), "deployit-license.lic")
        val content = Files.readString(deployitLicenseFile.toPath())
        return Base64.getEncoder().encodeToString(content.toByteArray())
    }

    open fun getOperatorImage(): String {
        return getProvider().operatorImage.value("xebialabs/deploy-operator").get()
    }

    open fun getStorageClass(): String {
        return getProvider().storageClass.value("standard").get()
    }

    abstract fun updateInfrastructure(infraInfo: InfrastructureInfo)

    abstract fun getProviderHomeDir(): String

    abstract fun getProvider(): Provider
}
