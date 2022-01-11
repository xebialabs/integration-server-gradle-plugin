package ai.digital.integration.server.common.cluster.operator

import ai.digital.integration.server.common.constant.OperatorProviderName
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.InfrastructureInfo
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.domain.profiles.OperatorProfile
import ai.digital.integration.server.common.domain.providers.operator.Provider
import ai.digital.integration.server.common.util.*
import ai.digital.integration.server.deploy.internals.CliUtil
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.internals.WorkerUtil
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import ai.digital.integration.server.release.internals.ReleaseExtensionUtil
import ai.digital.integration.server.release.tasks.cluster.ReleaseClusterUtil
import ai.digital.integration.server.release.util.ReleaseServerUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.gradle.api.Project
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

@Suppress("UnstableApiUsage")
abstract class OperatorHelper(val project: Project, val productName: ProductName) {

    var loggingJob: Job? = null

    val OPERATOR_INFRASTRUCTURE_PATH = "digitalai-${getName()}/infrastructure.yaml"

    val OPERATOR_CR_VALUES_REL_PATH = "digitalai-${getName()}/kubernetes/dai${getName()}_cr.yaml"

    private val operatorMetadataPath = "${getName()}/operator/operator-metadata.properties"

    private val OPERATOR_FOLDER_NAME: String = "xl-${getName()}-kubernetes-operator"

    private val OPERATOR_APPS_REL_PATH = "digitalai-${getName()}/applications.yaml"

    private val OPERATOR_CR_PACKAGE_REL_PATH = "digitalai-${getName()}/deployment-cr.yaml"

    private val OPERATOR_PACKAGE_REL_PATH = "digitalai-${getName()}/deployment.yaml"

    private val DIGITAL_AI_PATH = "digital-ai.yaml"

    companion object {
        fun getOperatorHelper(project: Project, productName: ProductName): OperatorHelper {
            return when (val providerName = getOperatorProvider(project, productName)) {
                OperatorProviderName.AWS_EKS.providerName -> AwsEksHelper(project, productName)
                OperatorProviderName.AWS_OPENSHIFT.providerName -> AwsOpenshiftHelper(project, productName)
                OperatorProviderName.AZURE_AKS.providerName -> AzureAksHelper(project, productName)
                OperatorProviderName.GCP_GKE.providerName -> GcpGkeHelper(project, productName)
                OperatorProviderName.ON_PREMISE.providerName -> OnPremHelper(project, productName)
                OperatorProviderName.VMWARE_OPENSHIFT.providerName -> VmwareOpenshiftHelper(project, productName)
                else -> {
                    throw IllegalArgumentException("Provided operator provider name `$providerName` is not supported. Choose one of ${
                        OperatorProviderName.values().joinToString()
                    }")
                }
            }
        }

        private fun getOperatorProvider(project: Project, productName: ProductName): String {
            return when (productName) {
                ProductName.DEPLOY -> DeployClusterUtil.getOperatorProvider(project)
                ProductName.RELEASE -> ReleaseClusterUtil.getOperatorProvider(project)
            }
        }
    }

    fun getOperatorHomeDir(): String =
        project.buildDir.toPath().resolve(OPERATOR_FOLDER_NAME).toAbsolutePath().toString()

    private fun getProviderWorkDir(): String =
        project.buildDir.toPath().resolve("${getProvider().name.get()}-work").toAbsolutePath().toString()

    fun getProfile(): OperatorProfile {
        return when (productName) {
            ProductName.DEPLOY -> DeployExtensionUtil.getExtension(project).clusterProfiles.operator()
            ProductName.RELEASE -> ReleaseExtensionUtil.getExtension(project).clusterProfiles.operator()
        }
    }

    fun updateOperatorApplications() {
        project.logger.lifecycle("Updating operator's applications")

        val file = File(getProviderHomeDir(), OPERATOR_APPS_REL_PATH)
        val pairs = mutableMapOf<String, Any>("spec[0].children[0].name" to getProvider().operatorPackageVersion.get())
        YamlFileUtil.overlayFile(file, pairs)
    }

    fun updateOperatorDeployment() {
        project.logger.lifecycle("Updating operator's deployment")

        val file = File(getProviderHomeDir(), OPERATOR_PACKAGE_REL_PATH)
        val pairs =
            mutableMapOf<String, Any>("spec.package" to "Applications/${getPrefixName()}-operator-app/${getProvider().operatorPackageVersion.get()}")
        YamlFileUtil.overlayFile(file, pairs)
    }

    fun updateOperatorDeploymentCr() {
        project.logger.lifecycle("Updating operator's deployment CR")

        val file = File(getProviderHomeDir(), OPERATOR_CR_PACKAGE_REL_PATH)
        val pairs =
            mutableMapOf<String, Any>("spec.package" to "Applications/${getPrefixName()}-cr/${getProvider().operatorPackageVersion.get()}")
        YamlFileUtil.overlayFile(file, pairs)
    }

    fun turnOnLogging() {
        loggingJob = GlobalScope.launch {
            repeat(1000) {
                getKubectlHelper().savePodLogs(getPostgresPodName(0))
                getKubectlHelper().savePodLogs(getRabbitMqPodName(0))
                List(getMasterCount()) { position -> getMasterPodName(position) }.forEach {
                    getKubectlHelper().savePodLogs(it)
                }
                List(getWorkerCount()) { position -> getWorkerPodName(position) }.forEach {
                    getKubectlHelper().savePodLogs(it)
                }
                delay(2000L) // make it configurable
            }
        }
    }

    fun turnOffLogging() {
        loggingJob?.cancel()
    }

    fun waitForDeployment() {
        val resources = if (hasIngress()) arrayOf("deployment.apps/${getPrefixName()}-operator-controller-manager",
            "deployment.apps/dai-${getPrefixName()}-nginx-ingress-controller",
            "deployment.apps/dai-${getPrefixName()}-nginx-ingress-controller-default-backend")
        else
            arrayOf("deployment.apps/${getPrefixName()}-operator-controller-manager")

        resources.forEach { resource ->
            if (!getKubectlHelper().wait(resource, "Available", getProfile().deploymentTimeoutSeconds.get())) {
                throw RuntimeException("Resource $resource  is not available")
            }
        }
    }

    fun waitForMasterPods() {
        val resources = List(getMasterCount()) { position -> getMasterPodName(position) }

        resources.forEach { resource ->
            if (!getKubectlHelper().wait(resource, "Ready", getProfile().deploymentTimeoutSeconds.get())) {
                throw RuntimeException("Resource $resource is not ready")
            }
        }
    }

    fun waitForWorkerPods() {
        val resources = List(getWorkerCount()) { position -> getWorkerPodName(position) }
        resources.forEach { resource ->
            if (!getKubectlHelper().wait(resource, "Ready", getProfile().deploymentTimeoutSeconds.get())) {
                throw RuntimeException("Resource $resource is not ready")
            }
        }
    }

    fun createClusterMetadata() {
        val path = IntegrationServerUtil.getRelativePathInIntegrationServerDist(project, operatorMetadataPath)
        path.parent.toFile().mkdirs()
        val props = Properties()
        props["cluster.port"] = getPort()
        props["cluster.context-root"] = getContextRoot()
        props["cluster.host"] = getHost()
        props["cluster.fqdn"] = getFqdn()
        PropertiesUtil.writePropertiesFile(path.toFile(), props)
    }

    fun waitForBoot() {
        val url = when (productName) {
            ProductName.DEPLOY -> "http://${getFqdn()}/deployit/metadata/type"
            ProductName.RELEASE -> "http://${getFqdn()}/api/extension/metadata"
        }
        val server = DeployServerUtil.getServer(project)
        WaitForBootUtil.byPort(project, getName(), url, null, server.pingRetrySleepTime, server.pingTotalTries)
    }

    fun undeployCluster() {

        project.logger.lifecycle("Operator is being undeployed")

        if (undeployCis()) {
            project.logger.lifecycle("PVCs are being deleted")
            getKubectlHelper().deleteAllPVCs()
        } else {
            project.logger.lifecycle("Skip delete of PVCs")
        }
    }

    fun undeployCis(): Boolean {
        val fileStream = {}::class.java.classLoader.getResourceAsStream("operator/python/undeploy.py")
        val resultComposeFilePath = Paths.get(getProviderWorkDir(), "undeploy.py")
        fileStream?.let {
            FileUtil.copyFile(it, resultComposeFilePath)
        }
        return try {
            CliUtil.executeScripts(project,
                listOf(resultComposeFilePath.toFile()),
                "undeploy.py",
                auxiliaryServer = true)
            true
        } catch (e: RuntimeException) {
            project.logger.error("Undeploy didn't run. Check if operator's ${getName()} server is running on port ${getOperatorDeployServer(project).httpPort}: ${e.message}")
            false
        } catch (e: IOException) {
            project.logger.error("Undeploy didn't run. Check if operator's ${getName()} server has all files: ${e.message}")
            false
        }
    }

    open fun getOperatorImage(): String {
        return getProvider().operatorImage.getOrElse("xebialabs/${getName()}-operator:1.2.0")
    }

    fun updateOperatorCrValues() {
        project.logger.lifecycle("Updating operator's CR values")

        val file = File(getProviderHomeDir(), OPERATOR_CR_VALUES_REL_PATH)
        val pairs =
            mutableMapOf<String, Any>(
                "spec.ImageRepository" to getImageRepository(),
                "spec.ImageTag" to getServerVersion(),
                "spec.KeystorePassphrase" to getProvider().keystorePassphrase.get(),
                "spec.Persistence.StorageClass" to getStorageClass(),
                "spec.RepositoryKeystore" to getProvider().repositoryKeystore.get(),
                "spec.postgresql.image.debug" to true,
                "spec.postgresql.persistence.size" to "5Gi",
                "spec.postgresql.persistence.storageClass" to getDbStorageClass(),
                "spec.postgresql.postgresqlMaxConnections" to "500",
                "spec.keycloak.postgresql.postgresqlMaxConnections" to "500",
                "spec.keycloak.install" to false,
                "spec.oidc.enabled" to false,
                "spec.rabbitmq.persistence.storageClass" to getMqStorageClass(),
                "spec.rabbitmq.image.debug" to true,
                "spec.rabbitmq.image.tag" to "3.9.8-debian-10-r6", // original one is slow and unstable
                "spec.rabbitmq.persistence.size" to "1Gi",
                "spec.rabbitmq.replicaCount" to getProvider().rabbitmqReplicaCount.get(),
                "spec.rabbitmq.extraConfiguration" to
                        listOf(
                            "load_definitions = /app/${getPrefixName()}-load_definition.json",
                            "raft.wal_max_size_bytes = 1048576"
                        ).joinToString(separator = "\n", postfix = "\n"),
                "spec.rabbitmq.persistence.replicaCount" to 1,
                "spec.route.hosts" to arrayOf(getHost()),
                "spec.${getPrefixName()}License" to getLicense()
            )

        when (productName) {
            ProductName.DEPLOY -> {
                pairs.putAll(mutableMapOf<String, Any>(
                    "spec.XldMasterCount" to getMasterCount(),
                    "spec.XldWorkerCount" to getWorkerCount(),
                    "spec.Persistence.XldMasterPvcSize" to "1Gi",
                    "spec.Persistence.XldWorkerPvcSize" to "1Gi"
                ))
            }
            ProductName.RELEASE -> {
                pairs.putAll(mutableMapOf<String, Any>(
                    "spec.Persistence.Size" to "1Gi"
                ))
            }
        }

        YamlFileUtil.overlayFile(file, pairs, minimizeQuotes = false)
    }

    private fun getImageRepository(): String {
        return getServer().dockerImage!!
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

    private fun getLicense(): String {
        val licenseFileName = when (productName) {
            ProductName.DEPLOY -> "deployit-license.lic"
            ProductName.RELEASE -> "xl-release-license.lic"
        }
        val licenseFile = File(getConfigDir(), licenseFileName)
        val content = Files.readString(licenseFile.toPath())
        return Base64.getEncoder().encodeToString(content.toByteArray())
    }

    open fun getWorkerCount(): Int {
        return WorkerUtil.getNumberOfWorkers(project)
    }

    open fun getStorageClass(): String {
        return getProvider().storageClass.getOrElse("standard")
    }

    open fun getDbStorageClass(): String {
        return getStorageClass()
    }

    open fun getMqStorageClass(): String {
        return getStorageClass()
    }

    open fun getFqdn(): String {
        return getProvider().host.getOrElse(getProvider().name.get())
    }

    open fun getContextRoot(): String {
        return "/"
    }

    open fun getCurrentContextInfo(): InfrastructureInfo {
        return InfrastructureInfo(null, null, null, null, null, null)
    }

    open fun getHost(): String {
        return getProvider().host.getOrElse(getProvider().name.get())
    }

    open fun getPort(): String {
        return "80"
    }

    open fun applyYamlFiles() {
        project.logger.lifecycle("Applying prepared Yaml files")

        val digitalAiPath = File(getProviderHomeDir(), DIGITAL_AI_PATH)
        project.logger.lifecycle("Applying Digital AI $productName platform on cluster ($digitalAiPath)")
        XlCliUtil.xlApply(project, digitalAiPath, getProfile().xlCliVersion.get(), File(getProviderHomeDir()), getOperatorDeployServer(project).httpPort)
    }

    abstract fun getProviderHomeDir(): String

    abstract fun getProvider(): Provider

    open fun getKubectlHelper(): KubeCtlHelper = KubeCtlHelper(project)

    open fun hasIngress(): Boolean = true

    open fun getWorkerPodName(position: Int) = "pod/dai-${getPrefixName()}-digitalai-${getName()}-worker-$position"

    open fun getMasterPodName(position: Int) = "pod/dai-${getPrefixName()}-digitalai-${getName()}-master-$position"

    open fun getPostgresPodName(position: Int) = "pod/dai-${getPrefixName()}-postgresql-$position"

    open fun getRabbitMqPodName(position: Int) = "pod/dai-${getPrefixName()}-rabbitmq-$position"

    fun getPrefixName(): String {
        return when (productName) {
            ProductName.DEPLOY -> "xld"
            ProductName.RELEASE -> "xlr"
        }
    }

    fun getTemplate(relativePath: String): File {
        val file = File(relativePath)
        val fileStream = {}::class.java.classLoader.getResourceAsStream(relativePath)
        val resultComposeFilePath = Paths.get(getProviderWorkDir(), file.name)
        fileStream?.let {
            FileUtil.copyFile(it, resultComposeFilePath)
        }
        return resultComposeFilePath.toFile()
    }

    open fun getMasterCount(): Int {
        return when (productName) {
            ProductName.DEPLOY -> DeployServerUtil.getServers(project).size
            ProductName.RELEASE -> ReleaseExtensionUtil.getExtension(project).servers.size
        }
    }

    private fun getConfigDir(): File {
        return when (productName) {
            ProductName.DEPLOY -> DeployServerUtil.getConfDir(project)
            ProductName.RELEASE -> ReleaseServerUtil.getConfDir(project)
        }
    }

    fun getName(): String {
        return productName.toString().toLowerCase()
    }

    fun getOperatorDeployServer(project: Project): Server {
        val server = DeployServerUtil.getOperatorDeployServer(project)
        if (DeployServerUtil.getResolvedDockerFile(project, server).toFile().isFile) {
            val httpPort = DeployServerUtil.getDockerContainerPort(project, server, 4516)
            httpPort?.let {
                server.httpPort = httpPort
            }
        }
        return server
    }
}
