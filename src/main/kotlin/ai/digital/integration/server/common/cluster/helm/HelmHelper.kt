package ai.digital.integration.server.common.cluster.helm

import ai.digital.integration.server.common.cluster.Helper
import ai.digital.integration.server.common.cluster.util.OperatorUtil
import ai.digital.integration.server.common.constant.OperatorHelmProviderName
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.constant.ServerConstants
import ai.digital.integration.server.common.domain.InfrastructureInfo
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.domain.profiles.HelmProfile
import ai.digital.integration.server.common.domain.profiles.IngressType
import ai.digital.integration.server.common.domain.profiles.OperatorProfile
import ai.digital.integration.server.common.domain.providers.Provider
import ai.digital.integration.server.common.util.*
import ai.digital.integration.server.deploy.domain.Worker
import ai.digital.integration.server.deploy.internals.CliUtil
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.internals.WorkerUtil
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import ai.digital.integration.server.release.internals.ReleaseExtensionUtil
import ai.digital.integration.server.release.tasks.cluster.ReleaseClusterUtil
import ai.digital.integration.server.release.util.ReleaseServerUtil
import kotlinx.coroutines.*
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.api.provider.Property
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@Suppress("UnstableApiUsage")
abstract class HelmHelper(project: Project, productName: ProductName) : Helper(project, productName) {

    private val HELM_FOLDER_NAME: String = "xl-${getName()}-kubernetes-helm-chart"

    private val VALUES_NGINX_PATH = "values-nginx.yaml"
    private val VALUES_HAPROXY_PATH = "values-haproxy.yaml"
    private val VALUES_PATH = "values.yaml"

    companion object {
        fun getHelmHelper(project: Project): HelmHelper {
            val productName = if (ReleaseServerUtil.isReleaseServerDefined(project)) {
                ProductName.RELEASE
            } else {
                ProductName.DEPLOY
            }
            return getHelmHelper(project, productName)
        }

        fun getHelmHelper(project: Project, productName: ProductName): HelmHelper {
            return when (val providerName = getHelmProvider(project, productName)) {
                OperatorHelmProviderName.AWS_EKS.providerName -> AwsEksHelmHelper(project, productName)
                OperatorHelmProviderName.AWS_OPENSHIFT.providerName -> AwsOpenshiftHelmHelper(project, productName)
                OperatorHelmProviderName.AZURE_AKS.providerName -> AzureAksHelmHelper(project, productName)
                OperatorHelmProviderName.GCP_GKE.providerName -> GcpGkeHelmHelper(project, productName)
                OperatorHelmProviderName.ON_PREMISE.providerName -> OnPremHelmHelper(project, productName)
                OperatorHelmProviderName.VMWARE_OPENSHIFT.providerName -> VmwareOpenshiftHelmHelper(project, productName)
                else -> {
                    throw IllegalArgumentException("Provided operator provider name `$providerName` is not supported. Choose one of ${
                        OperatorHelmProviderName.values().joinToString()
                    }")
                }
            }
        }

        private fun getHelmProvider(project: Project, productName: ProductName): String {
            return when (productName) {
                ProductName.DEPLOY -> DeployClusterUtil.getHelmProvider(project)
                ProductName.RELEASE -> ReleaseClusterUtil.getHelmProvider(project)
            }
        }
    }

    fun getHelmHomeDir(): String =
            project.buildDir.toPath().resolve(HELM_FOLDER_NAME).toAbsolutePath().toString()


    fun getProfile(): HelmProfile {
        return when (productName) {
            ProductName.DEPLOY -> DeployExtensionUtil.getExtension(project).clusterProfiles.helm()
            ProductName.RELEASE -> ReleaseExtensionUtil.getExtension(project).clusterProfiles.helm()
        }
    }

    fun copyValuesYamlFile() {
        when (IngressType.valueOf(getProfile().ingressType.get())) {
            IngressType.NGINX -> {
                val fileNginxValues = File(getHelmHomeDir(), VALUES_NGINX_PATH)
                project.logger.lifecycle("Copying $VALUES_NGINX_PATH file to values.yaml}")
                ProcessUtil.executeCommand(
                        "cp -f \"$fileNginxValues\" \"${getHelmValuesFile()}\"", logOutput = false)
            }
            IngressType.HAPROXY -> {
                val fileHaproxyValues = File(getHelmHomeDir(), VALUES_HAPROXY_PATH)
                project.logger.lifecycle("Copying $VALUES_HAPROXY_PATH file to values.yaml}")
                ProcessUtil.executeCommand(
                        "cp -f \"$fileHaproxyValues\" \"${getHelmValuesFile()}\"", logOutput = false)
            }
        }

    }

    fun getHelmValuesFile(): File {
        return File(getHelmHomeDir(), VALUES_PATH)
    }

    open fun updateHelmValuesYaml() {
        project.logger.lifecycle("Updating Helm values")

        val file = getHelmValuesFile()
        val pairs =
                mutableMapOf<String, Any>(
                        //"spec.ImageRepository" to getServerImageRepository(),
                        "AdminPassword" to "admin",
                        "ServerImageRepository" to getServerImageRepository(),
                        "ImageTag" to getServerVersion(),
                        "centralConfiguration.image.repository" to getCentralConfigImageRepository(),
                        "KeystorePassphrase" to getProvider().keystorePassphrase.get(),
                        "Persistence.StorageClass" to getStorageClass(),
                        "RepositoryKeystore" to getProvider().repositoryKeystore.get(),
                        "postgresql.image.debug" to true,
                        "postgresql.persistence.size" to "1Gi",
                        "postgresql.persistence.storageClass" to getDbStorageClass(),
                        "postgresql.postgresqlMaxConnections" to getDbConnectionCount(),
                        "keycloak.install" to false,
                        "keycloak.postgresql.persistence.size" to "1Gi",
                        "oidc.enabled" to false,
                        "rabbitmq.persistence.storageClass" to getMqStorageClass(),
                        "rabbitmq.image.debug" to true,
                        "rabbitmq.persistence.size" to "1Gi",
                        "rabbitmq.replicaCount" to getProvider().rabbitmqReplicaCount.get(),
                        "rabbitmq.persistence.replicaCount" to 1,
                        "${getPrefixName()}License" to getLicense()
                )

        if (IngressType.valueOf(getProfile().ingressType.get()) == IngressType.HAPROXY) {
            pairs.putAll(mutableMapOf<String, Any>(
                    "haproxy-ingress.install" to true,
                    "haproxy-ingress.service.type" to "LoadBalancer",
                    "nginx-ingress-controller.install" to false
            ))
        } else {
            pairs.putAll(mutableMapOf<String, Any>(
                    "nginx-ingress-controller.service.type" to "LoadBalancer"
            ))
        }

        when (productName) {
            ProductName.DEPLOY -> {
                pairs.putAll(mutableMapOf<String, Any>(
                        "XldMasterCount" to getMasterCount(),
                        "XldWorkerCount" to getDeployWorkerCount(),
                        "WorkerImageRepository" to getDeployWorkerImageRepository(),
                        "Persistence.XldMasterPvcSize" to "1Gi",
                        "Persistence.XldWorkerPvcSize" to "1Gi"
                ))
            }
            ProductName.RELEASE -> {
                pairs.putAll(mutableMapOf<String, Any>(
                        ".replicaCount" to getMasterCount(),
                        ".Persistence.Size" to "1Gi"
                ))
            }
        }
        YamlFileUtil.overlayFile(file, pairs, minimizeQuotes = false)
        updateCustomHelmValues(file)
    }

    fun updateHelmDependency() {
        ProcessUtil.executeCommand("helm dependency update \"${getHelmHomeDir()}\"")
    }

    fun installCluster() {
        uninstallExistingHelmReleaseFromCluster()
        ProcessUtil.executeCommand("helm install ${getHelmReleaseName()} \"${getHelmHomeDir()}\"")
    }

    fun undeployCluster() {
        project.logger.lifecycle("Release ${getHelmReleaseName()} is being uninstalled")
        uninstallExistingHelmReleaseFromCluster()
        project.logger.lifecycle("PVCs are being deleted")
        getKubectlHelper().deleteAllPVCs()
    }

    private fun uninstallExistingHelmReleaseFromCluster() {
        ProcessUtil.executeCommand("helm uninstall ${getHelmReleaseName()}", throwErrorOnFailure= false)
    }

    private fun getHelmReleaseName(): String {
        return when (productName) {
            ProductName.DEPLOY -> getProvider().helmXldReleaseName.get()
            ProductName.RELEASE -> getProvider().helmXlrReleaseName.get()
        }
    }

    abstract fun updateCustomHelmValues(valuesFile: File)

    open fun getContextPath(): String = "ingress.path"

    open fun getContextRoot(): String {
        val file = getHelmValuesFile()
        val pathKey = getContextPath()
        return getContextRootPath(file, pathKey)
    }

    override fun getPort(): String {
        return "80"
    }

    override fun getFqdn(): String = getHost()
}

