package ai.digital.integration.server.common.cluster.operator

import ai.digital.integration.server.common.cluster.Helper
import ai.digital.integration.server.common.cluster.util.OperatorUtil
import ai.digital.integration.server.common.constant.OperatorHelmProviderName
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.constant.ServerConstants
import ai.digital.integration.server.common.domain.InfrastructureInfo
import ai.digital.integration.server.common.domain.Server
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
abstract class OperatorHelper(project: Project, productName: ProductName) : Helper(project, productName){

    var loggingJob: Job? = null

    val OPERATOR_INFRASTRUCTURE_PATH = "digitalai-${getName()}/infrastructure.yaml"

    val OPERATOR_CR_VALUES_FILENAME = "dai${getName()}_cr.yaml"

    val OPERATOR_CR_VALUES_REL_PATH = "digitalai-${getName()}/kubernetes/$OPERATOR_CR_VALUES_FILENAME"

    private val operatorMetadataPath = "${getName()}/operator/operator-metadata.properties"

    private val OPERATOR_FOLDER_NAME: String = "xl-${getName()}-kubernetes-operator"

    private val OPERATOR_APPS_REL_PATH = "digitalai-${getName()}/applications.yaml"

    private val OPERATOR_CR_PACKAGE_REL_PATH = "digitalai-${getName()}/deployment-cr.yaml"

    private val OPERATOR_PACKAGE_REL_PATH = "digitalai-${getName()}/deployment.yaml"

    private val OPERATOR_ENVIRONMENT_REL_PATH = "digitalai-${getName()}/environment.yaml"

    val OPERATOR_DEPLOYMENT_PATH = "digitalai-${getName()}/kubernetes/template/deployment.yaml"

    private val DIGITAL_AI_PATH = "digital-ai.yaml"

    companion object {
        fun getOperatorHelper(project: Project): OperatorHelper {
            val productName = if (ReleaseServerUtil.isReleaseServerDefined(project)) {
                ProductName.RELEASE
            } else {
                ProductName.DEPLOY
            }
            return getOperatorHelper(project, productName)
        }

        fun getOperatorHelper(project: Project, productName: ProductName): OperatorHelper {
            return when (val providerName = getOperatorProvider(project, productName)) {
                OperatorHelmProviderName.AWS_EKS.providerName -> AwsEksOperatorHelper(project, productName)
                OperatorHelmProviderName.AWS_OPENSHIFT.providerName -> AwsOpenshiftOperatorHelper(project, productName)
                OperatorHelmProviderName.AZURE_AKS.providerName -> AzureAksOperatorHelper(project, productName)
                OperatorHelmProviderName.GCP_GKE.providerName -> GcpGkeOperatorHelper(project, productName)
                OperatorHelmProviderName.ON_PREMISE.providerName -> OnPremOperatorHelper(project, productName)
                OperatorHelmProviderName.VMWARE_OPENSHIFT.providerName -> VmwareOpenshiftOperatorHelper(project, productName)
                else -> {
                    throw IllegalArgumentException("Provided operator provider name `$providerName` is not supported. Choose one of ${
                        OperatorHelmProviderName.values().joinToString()
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



    fun getProfile(): OperatorProfile {
        return when (productName) {
            ProductName.DEPLOY -> DeployExtensionUtil.getExtension(project).clusterProfiles.operator()
            ProductName.RELEASE -> ReleaseExtensionUtil.getExtension(project).clusterProfiles.operator()
        }
    }

    fun updateOperatorApplications() {
        project.logger.lifecycle("Updating operator's applications")

        val operatorNamespaceVersion = getOperatorNamespaceVersion()

        val file = File(getProviderHomeDir(), OPERATOR_APPS_REL_PATH)
        val pairs = mutableMapOf<String, Any>("spec[0].children[0].name" to operatorNamespaceVersion)
        YamlFileUtil.overlayFile(file, pairs)
    }

    fun updateOperatorEnvironment() {
        project.logger.lifecycle("Updating operator's environment")

        val operatorNamespace = getNamespace()?.let { "-$it" } ?: ""

        val file = File(getProviderHomeDir(), OPERATOR_ENVIRONMENT_REL_PATH)
        val pairs =
            mutableMapOf<String, Any>(
                "spec[0].children[0].name" to "${getPrefixName()}$operatorNamespace"
            )
        YamlFileUtil.overlayFile(file, pairs)
    }

    fun updateOperatorDeployment() {
        project.logger.lifecycle("Updating operator's deployment")

        val operatorNamespace = getNamespace()?.let { "-$it" } ?: ""
        val operatorNamespaceVersion = getOperatorNamespaceVersion()

        val file = File(getProviderHomeDir(), OPERATOR_PACKAGE_REL_PATH)
        val pairs =
            mutableMapOf<String, Any>(
                "spec.package" to "Applications/${getPrefixName()}-operator-app/$operatorNamespaceVersion",
                "spec.environment" to "Environments/kubernetes-envs/${getPrefixName()}$operatorNamespace"
            )
        YamlFileUtil.overlayFile(file, pairs)
    }

    fun updateOperatorDeploymentCr() {
        project.logger.lifecycle("Updating operator's deployment CR")

        val operatorNamespace = getNamespace()?.let { "-$it" } ?: ""
        val operatorNamespaceVersion = getOperatorNamespaceVersion()

        val file = File(getProviderHomeDir(), OPERATOR_CR_PACKAGE_REL_PATH)
        val pairs =
            mutableMapOf<String, Any>(
                "spec.package" to "Applications/${getPrefixName()}-cr/$operatorNamespaceVersion",
                "spec.environment" to "Environments/kubernetes-envs/${getPrefixName()}$operatorNamespace"
            )
        YamlFileUtil.overlayFile(file, pairs)
    }

    private fun getOperatorNamespaceVersion(): String {
        val namespace = getNamespace()?.let { "-$it" } ?: ""
        return getProvider().operatorPackageVersion
            .map { "$it$namespace" }
            .getOrElse("${getServerVersion()}$namespace")
    }

    fun turnOnLogging() {
        loggingJob = GlobalScope.launch {
            repeat(1000) {
                getKubectlHelper().savePodLogs(getPostgresPodName(0))
                getKubectlHelper().savePodLogs(getRabbitMqPodName(0))
                List(getMasterCount()) { position -> getMasterPodName(position) }.forEach {
                    getKubectlHelper().savePodLogs(it)
                }

                if (productName == ProductName.DEPLOY) {
                    List(getDeployWorkerCount()) { position -> getWorkerPodName(position) }.forEach {
                        getKubectlHelper().savePodLogs(it)
                    }
                }
                delay(2000L) // make it configurable
            }
        }
    }

    fun turnOffLogging() {
        loggingJob?.cancel()
    }

    fun waitForDeployment() {
        val namespaceAsPrefix = getNamespace()?.let { "$it-" } ?: ""
        val resources = if (hasIngress()) {
            when (IngressType.valueOf(getProfile().ingressType.get())) {
                IngressType.NGINX ->
                    arrayOf(
                        "deployment.apps/${namespaceAsPrefix}dai-${getPrefixName()}-nginx-ingress-controller",
                        "deployment.apps/${namespaceAsPrefix}dai-${getPrefixName()}-nginx-ingress-controller-default-backend"
                    )
                IngressType.HAPROXY ->
                    arrayOf("deployment.apps/${namespaceAsPrefix}dai-${getPrefixName()}-haproxy-ingress")
            }
        } else
            arrayOf()

        (resources + "deployment.apps/${getPrefixName()}-operator-controller-manager").forEach { resource ->
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
        val resources = List(getDeployWorkerCount()) { position -> getWorkerPodName(position) }
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
        val contextRoot = when (getContextRoot() == "/") {
            true -> ""
            false -> getContextRoot()
        }

        val url = when (productName) {
            ProductName.DEPLOY -> "http://${getFqdn()}${contextRoot}/deployit/metadata/type"
            ProductName.RELEASE -> "http://${getFqdn()}${contextRoot}/api/extension/metadata"
        }
        val server = ServerUtil(project, productName).getServer()
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
        val scriptName = "undeploy_${productName.displayName}.py"
        val fileStream = {}::class.java.classLoader.getResourceAsStream("operator/python/$scriptName")
        val resultComposeFilePath = Paths.get(getProviderWorkDir(), scriptName)
        fileStream?.let {
            FileUtil.copyFile(it, resultComposeFilePath)
        }
        return try {
            CliUtil.executeScripts(project,
                listOf(resultComposeFilePath.toFile()),
                scriptName,
                auxiliaryServer = true)
            true
        } catch (e: RuntimeException) {
            project.logger.error("Undeploy didn't run. Check if operator's ${getName()} server is running on port ${
                OperatorUtil(project).getOperatorServer().httpPort
            }: ${e.message}")
            false
        } catch (e: IOException) {
            project.logger.error("Undeploy didn't run. Check if operator's ${getName()} server has all files: ${e.message}")
            false
        }
    }

    open fun getOperatorImage(): String? {
        // it needs to be aligned with operatorBranch default value
        return getProvider().operatorImage.orNull
    }

    fun updateDeploymentValues() {
        getOperatorImage()?.let { operatorImage ->
            project.logger.lifecycle("Updating operator's deployment values")
            val file = File(getProviderHomeDir(), OPERATOR_DEPLOYMENT_PATH)
            val pairs =
                mutableMapOf<String, Any>(
                    "spec.template.spec.containers[1].image" to operatorImage
                )
            YamlFileUtil.overlayFile(file, pairs, minimizeQuotes = false)
        }
    }

    fun updateOperatorCrValues() {
        project.logger.lifecycle("Updating operator's CR values")

        val file = getInitialCrValuesFile()
        val pairs =
            mutableMapOf<String, Any>(
                "spec.ImageRepository" to getServerImageRepository(),
                "spec.ServerImageRepository" to getServerImageRepository(),
                "spec.ImageTag" to getServerVersion(),
                "spec.KeystorePassphrase" to getProvider().keystorePassphrase.get(),
                "spec.Persistence.StorageClass" to getStorageClass(),
                "spec.RepositoryKeystore" to getProvider().repositoryKeystore.get(),
                "spec.postgresql.image.debug" to true,
                "spec.postgresql.persistence.size" to "1Gi",
                "spec.postgresql.persistence.storageClass" to getDbStorageClass(),
                "spec.postgresql.postgresqlMaxConnections" to getDbConnectionCount(),
                "spec.keycloak.install" to false,
                "spec.keycloak.postgresql.persistence.size" to "1Gi",
                "spec.oidc.enabled" to false,
                "spec.rabbitmq.persistence.storageClass" to getMqStorageClass(),
                "spec.rabbitmq.image.debug" to true,
                "spec.rabbitmq.persistence.size" to "1Gi",
                "spec.rabbitmq.replicaCount" to getProvider().rabbitmqReplicaCount.get(),
                "spec.rabbitmq.persistence.replicaCount" to 1,
                "spec.route.hosts" to arrayOf(getHost()),
                "spec.${getPrefixName()}License" to getLicense()
            )

        if (IngressType.valueOf(getProfile().ingressType.get()) == IngressType.HAPROXY) {
            pairs.putAll(
                mutableMapOf(
                    "spec.haproxy-ingress.install" to true,
                    "spec.nginx-ingress-controller.install" to false,
                    "spec.ingress.path" to getContextRoot(),
                    "spec.ingress.annotations" to mapOf(
                        "kubernetes.io/ingress.class" to "haproxy",
                        "ingress.kubernetes.io/ssl-redirect" to false,
                        "ingress.kubernetes.io/rewrite-target" to getContextRoot(),
                        "ingress.kubernetes.io/affinity" to "cookie",
                        "ingress.kubernetes.io/session-cookie-name" to "JSESSIONID",
                        "ingress.kubernetes.io/session-cookie-strategy" to "prefix",
                        "ingress.kubernetes.io/config-backend" to "option httpchk GET /ha/health HTTP/1.0"
                    )
                )
            )
        }

        when (productName) {
            ProductName.DEPLOY -> {
                pairs.putAll(mutableMapOf<String, Any>(
                    "spec.XldMasterCount" to getMasterCount(),
                    "spec.XldWorkerCount" to getDeployWorkerCount(),
                    "spec.WorkerImageRepository" to getDeployWorkerImageRepository(),
                    "spec.Persistence.XldMasterPvcSize" to "1Gi",
                    "spec.Persistence.XldWorkerPvcSize" to "1Gi"
                ))
            }
            ProductName.RELEASE -> {
                pairs.putAll(mutableMapOf<String, Any>(
                    "spec.replicaCount" to getMasterCount(),
                    "spec.Persistence.Size" to "1Gi"
                ))
            }
        }

        YamlFileUtil.overlayFile(file, pairs, minimizeQuotes = false)
        updateCustomOperatorCrValues(file)
    }

    abstract fun updateCustomOperatorCrValues(crValuesFile: File)

    private fun getDbConnectionCount(): String {
        val defaultMaxDbConnections = when (productName) {
            ProductName.DEPLOY ->
                ServerConstants.DEPLOY_DB_CONNECTION_NUMBER * (getMasterCount() + getDeployWorkerCount())
            ProductName.RELEASE ->
                ServerConstants.RELEASE_DB_CONNECTION_NUMBER * getMasterCount()
        }
        return getProvider().maxDbConnections.getOrElse(defaultMaxDbConnections).toString()
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

    private fun getLicense(): String {
        val licenseFileName = when (productName) {
            ProductName.DEPLOY -> "deployit-license.lic"
            ProductName.RELEASE -> "xl-release-license.lic"
        }
        val licenseFile = File(getConfigDir(), licenseFileName)
        val content = Files.readString(licenseFile.toPath())
        return Base64.getEncoder().encodeToString(content.toByteArray())
    }

    open fun getDeployWorkerCount(): Int {
        return WorkerUtil.getNumberOfWorkers(project)
    }

    open fun getMqStorageClass(): String {
        return getStorageClass()
    }

    override fun getFqdn(): String = getHost()

    fun getInitialCrValuesFile(): File {
        return File(getProviderHomeDir(), OPERATOR_CR_VALUES_REL_PATH)
    }

    fun getReferenceCrValuesFile(): File {
        return File(getProviderWorkDir(), OPERATOR_CR_VALUES_FILENAME)
    }

    open fun getProviderCrContextPath(): String = "spec.ingress.path"

    open fun getContextRoot(): String {
        val file = getReferenceCrValuesFile()
        val pathKey = getProviderCrContextPath()
        val pathValue = YamlFileUtil.readFileKey(file, pathKey) as String
        val expectedPathValue = when (productName) {
            ProductName.DEPLOY -> "/xl-deploy"
            ProductName.RELEASE -> "/xl-release"
        }
        return if (pathValue.startsWith(expectedPathValue)) {
            expectedPathValue
        } else {
            "/"
        }
    }

    open fun getCurrentContextInfo(): InfrastructureInfo {
        return InfrastructureInfo(null, null, null, null, null, null)
    }

    override fun getHost(): String {
        return getProvider().host.getOrElse("${getProvider().name.get()}-${productName.shortName}-${getNamespace() ?: "default"}")
    }

    fun getNamespace(): String? = getProfile().namespace.orNull

    override fun getPort(): String {
        return "80"
    }

    open fun applyYamlFiles() {
        project.logger.lifecycle("Applying prepared Yaml files")

        val digitalAiPath = File(getProviderHomeDir(), DIGITAL_AI_PATH)
        project.logger.lifecycle("Applying Digital AI $productName platform on cluster ($digitalAiPath)")
        val operatorServer = OperatorUtil(project).getOperatorServer()
        val startTime = LocalDateTime.now()
        try {
            XlCliUtil.xlApply(
                project,
                digitalAiPath,
                File(getProviderHomeDir()),
                operatorServer.httpPort
            )
            // copy cr file for reference
            FileUtils.copyFileToDirectory(getInitialCrValuesFile(), File(getProviderWorkDir()))
        } finally {
            DeployServerUtil.saveServerLogsToFile(project, operatorServer, "deploy-${operatorServer.version}", startTime)
        }
    }

    fun getProviderHomeDir(): String = "${getOperatorHomeDir()}/${getProviderHomePath()}"

    abstract fun getProviderHomePath(): String

    abstract override fun getProvider(): Provider

    override fun getKubectlHelper(): KubeCtlHelper = KubeCtlHelper(project, getNamespace())

    open fun hasIngress(): Boolean = true

    open fun getMasterCount(): Int {
        return when (productName) {
            ProductName.DEPLOY -> DeployServerUtil.getServers(project).size
            ProductName.RELEASE -> ReleaseExtensionUtil.getExtension(project).servers.size
        }
    }

    open fun getWorkerPodName(position: Int) = "pod/dai-${getPrefixName()}-digitalai-${getName()}-worker-$position"

    open fun getMasterPodName(position: Int) =
        "pod/dai-${getPrefixName()}-digitalai-${getName()}-${getMasterPodNameSuffix(position)}"

    open fun getPostgresPodName(position: Int) = "pod/dai-${getPrefixName()}-postgresql-$position"

    open fun getRabbitMqPodName(position: Int) = "pod/dai-${getPrefixName()}-rabbitmq-$position"

    open fun getMasterPodNameSuffix(position: Int): String {
        return when (productName) {
            ProductName.DEPLOY -> "master-$position"
            ProductName.RELEASE -> "$position"
        }
    }

    private fun getConfigDir(): File {
        return when (productName) {
            ProductName.DEPLOY -> DeployServerUtil.getConfDir(project)
            ProductName.RELEASE -> ReleaseServerUtil.getConfDir(project)
        }
    }

    fun cleanUpCluster(waiting: Duration) {
        if (getProfile().doCleanup.get()) {

            val resourcesList = arrayOf(
                "crd",
                "all",
                "service",
                "roles",
                "roleBinding",
                "clusterRoles",
                "clusterRoleBinding",
                "ing",
                "ingressclass",
                "pvc",
                "configmap",
                "secret",
                "job"
            )

            runBlocking {
                for (iteration in 1..3) {
                    project.logger.lifecycle("Clean up cluster resources in namespace ${getKubectlHelper().namespace} iteration $iteration")

                    val deleteResourcesJob = launch {
                        withTimeout(waiting.toMillis()) {
                            runInterruptible(Dispatchers.IO) {
                                deleteAllResources(resourcesList)
                            }
                        }
                    }
                    if (waitDeleteAllResources(deleteResourcesJob, iteration, waiting, resourcesList)) {
                        break
                    }
                }
            }

            project.logger.lifecycle("Clean up cluster resources finished in namespace ${getKubectlHelper().namespace}")
        } else {
            project.logger.lifecycle("Skip up cluster resources in namespace ${getKubectlHelper().namespace}")
        }
    }

    private fun getResources(resourcesList: Array<String>): String {
        val kubectlHelper = getKubectlHelper()
        val productNames = ProductName.values()

        val resources: List<String> = resourcesList.flatMap { resource ->
            productNames.map { productName ->
                kubectlHelper.getResourceNames(resource, productName)
            }
        }

        return resources.joinToString(" ").trim()
    }

    private fun deleteAllResources(resourcesList: Array<String>) {
        deleteResources(resourcesList)

        // delete ingressclass
        val kubectlHelper = getKubectlHelper()
        val names = kubectlHelper.getResourceNames("ingressclass")
        if (names.isNotBlank()) {
            project.logger.lifecycle("Deleting resources ingressclass:\n $names")
            val deleteResult = kubectlHelper.deleteNames(names)
            if (deleteResult.isNotBlank()) {
                project.logger.lifecycle("Deleted resources ingressclass:\n $deleteResult")
            }
        }
    }

    private suspend fun waitDeleteAllResources(
        deleteResourcesJob: Job, iteration: Int, waiting: Duration, resourcesList: Array<String>) : Boolean {
        val repeat = 10
        for (i in 1..repeat) {
            project.logger.lifecycle("Waiting cleanup $i")
            if (deleteResourcesJob.isActive) {
                delay(waiting.toMillis() / repeat)
            } else {
                break
            }
        }

        val existingResourcesFromList = getResources(resourcesList)
        val hasResources = existingResourcesFromList.isNotBlank()
        return if (hasResources) {
            project.logger.lifecycle("Has more resources, cancelling in iteration $iteration: \n $existingResourcesFromList")
            deleteResourcesJob.cancel()
            false
        } else {
            project.logger.lifecycle("Clean up cluster resources finished in iteration $iteration in namespace ${getKubectlHelper().namespace}")
            true
        }
    }

    private fun deleteResources(resourcesList: Array<String>) {
        val kubectlHelper = getKubectlHelper()
        val productNames = ProductName.values()

        // delete all by product and resource
        resourcesList.forEach { resource ->
            productNames.forEach { productName ->
                val names = kubectlHelper.getResourceNames(resource, productName)
                if (names.isNotBlank()) {
                    project.logger.lifecycle("Deleting resources $resource on $productName:\n $names")
                    if (resource == "crd" || resource == "service") {
                        val result = kubectlHelper.clearCrFinalizers(names)
                        project.logger.lifecycle("Cleared finalizers for $resource on $productName:\n $result")
                    }
                    val deleteResult = kubectlHelper.deleteNames(names)
                    if (deleteResult.isNotBlank()) {
                        project.logger.lifecycle("Deleted resources $resource on $productName:\n $deleteResult")
                    }
                }
            }
        }
    }
}
