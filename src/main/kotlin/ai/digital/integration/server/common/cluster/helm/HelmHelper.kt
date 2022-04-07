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

    private fun getConfigDir(): File {
        return when (productName) {
            ProductName.DEPLOY -> DeployServerUtil.getConfDir(project)
            ProductName.RELEASE -> ReleaseServerUtil.getConfDir(project)
        }
    }

    fun copyValuesFile () {
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

    open fun updateHelmValues() {
        project.logger.lifecycle("Updating Helm values")

        val file = getHelmValuesFile()
        val pairs =
                mutableMapOf<String, Any>(
                        //"spec.ImageRepository" to getServerImageRepository(),
                        ".AdminPassword" to "admin",
                        ".ServerImageRepository" to getServerImageRepository(),
                        ".ImageTag" to getServerVersion(),
                        ".KeystorePassphrase" to getProvider().keystorePassphrase.get(),
                        ".Persistence.StorageClass" to getStorageClass(),
                        ".RepositoryKeystore" to getProvider().repositoryKeystore.get(),
                        ".postgresql.image.debug" to true,
                        ".postgresql.persistence.size" to "1Gi",
                        ".postgresql.persistence.storageClass" to getDbStorageClass(),
                        ".postgresql.postgresqlMaxConnections" to getDbConnectionCount(),
                        ".keycloak.install" to false,
                        ".keycloak.postgresql.persistence.size" to "1Gi",
                        ".oidc.enabled" to false,
                        ".rabbitmq.persistence.storageClass" to getMqStorageClass(),
                        ".rabbitmq.image.debug" to true,
                        ".rabbitmq.persistence.size" to "1Gi",
                        ".rabbitmq.replicaCount" to getProvider().rabbitmqReplicaCount.get(),
                        ".rabbitmq.persistence.replicaCount" to 1,
                        ".${getPrefixName()}License" to getLicense()
                )

        when (productName) {
            ProductName.DEPLOY -> {
                pairs.putAll(mutableMapOf<String, Any>(
                        ".XldMasterCount" to getMasterCount(),
                        ".XldWorkerCount" to getDeployWorkerCount(),
                        ".WorkerImageRepository" to getDeployWorkerImageRepository(),
                        ".Persistence.XldMasterPvcSize" to "1Gi",
                        ".Persistence.XldWorkerPvcSize" to "1Gi"
                ))
            }
            ProductName.RELEASE -> {
                pairs.putAll(mutableMapOf<String, Any>(
                        ".replicaCount" to getMasterCount(),
                        ".Persistence.Size" to "1Gi"
                ))
            }
        }
        updateYamlFile(file, pairs)
        updateCustomHelmValues(file)
       // YamlFileUtil.overlayFile(file, pairs, minimizeQuotes = false)
      //  updateCustomOperatorCrValues(file)
    }

    abstract fun updateCustomHelmValues(valuesFile: File)

    open fun getMqStorageClass(): String {
        return getStorageClass()
    }

    private fun getServerImageRepository(): String {
        return getServer().dockerImage!!
    }

    private fun getDeployWorkerImageRepository(): String {
        return getDeployWorker().dockerImage!!
    }

    private fun getServerVersion(): String {
        return getServer().version!!
    }

    private fun getServer(): Server {
        return when (productName) {
            ProductName.DEPLOY -> DeployServerUtil.getServer(project)
            ProductName.RELEASE -> ReleaseServerUtil.getServer(project)
        }
    }

    private fun getDeployWorker(): Worker {
        return WorkerUtil.getWorkers(project)[0]
    }

    private fun getDbConnectionCount(): String {
        val defaultMaxDbConnections = when (productName) {
            ProductName.DEPLOY ->
                ServerConstants.DEPLOY_DB_CONNECTION_NUMBER * (getMasterCount() + getDeployWorkerCount())
            ProductName.RELEASE ->
                ServerConstants.RELEASE_DB_CONNECTION_NUMBER * getMasterCount()
        }
        return getProvider().maxDbConnections.getOrElse(defaultMaxDbConnections).toString()
    }

    open fun getMasterCount(): Int {
        return when (productName) {
            ProductName.DEPLOY -> DeployServerUtil.getServers(project).size
            ProductName.RELEASE -> ReleaseExtensionUtil.getExtension(project).servers.size
        }
    }

    open fun getDeployWorkerCount(): Int {
        return WorkerUtil.getNumberOfWorkers(project)
    }

    private fun getLicense(): String {
        val licenseFileName = when (productName) {
            ProductName.DEPLOY -> "deployit-license.lic"
            ProductName.RELEASE -> "xl-release-license.lic"
        }
        val licenseFile = File(getConfigDir(), licenseFileName)
        val content = Files.readString(licenseFile.toPath())
        return Base64.getEncoder().encodeToString(content.toByteArray())
    }

    fun updateYamlFile(filePath: File, pairs: MutableMap<String, Any>) {
        pairs.forEach { entry ->
            project.logger.lifecycle("${entry.key} : ${entry.value}")
            var value1 = entry.value
            var command = "\'${entry.key} = \"${entry.value}\"\'"
            if (value1 is String){
                command = "\'${entry.key} = \"${entry.value}\"\'"
            } else if reflect.TypeOf(value1).Kind() == reflect.Bool {
                command = "\'${entry.key} = ${entry.value}\'"
            } else if reflect.TypeOf(value1).Kind() == reflect.Float64 {
                result = fmt.Sprintf("%f", value)
            } else if reflect.TypeOf(value1).Kind() == reflect.Int {
                result = fmt.Sprintf("%d", value)
            }
            project.logger.lifecycle("command -> $command")
            ProcessUtil.executeCommand("yq -i $command \"${getHelmValuesFile()}\"")
        }
    }
}

