package ai.digital.integration.server.common.cluster.operator

import ai.digital.integration.server.common.cluster.util.OperatorUtil
import ai.digital.integration.server.common.constant.OperatorProviderName
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.constant.ServerConstants
import ai.digital.integration.server.common.domain.InfrastructureInfo
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.domain.profiles.OperatorProfile
import ai.digital.integration.server.common.domain.providers.operator.Provider
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
abstract class OperatorHelper(val project: Project, val productName: ProductName) {

    var loggingJob: Job? = null

    val OPERATOR_INFRASTRUCTURE_PATH = "digitalai-${getName()}/infrastructure.yaml"

    val OPERATOR_CR_VALUES_FILENAME = "dai${getName()}_cr.yaml"

    val OPERATOR_CR_VALUES_REL_PATH = "digitalai-${getName()}/kubernetes/$OPERATOR_CR_VALUES_FILENAME"

    private val operatorMetadataPath = "${getName()}/operator/operator-metadata.properties"

    private val OPERATOR_FOLDER_NAME: String = "xl-${getName()}-kubernetes-operator"

    private val OPERATOR_APPS_REL_PATH = "digitalai-${getName()}/applications.yaml"

    private val OPERATOR_CR_PACKAGE_REL_PATH = "digitalai-${getName()}/deployment-cr.yaml"

    private val OPERATOR_PACKAGE_REL_PATH = "digitalai-${getName()}/deployment.yaml"

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

    fun getProviderWorkDir(): String {
        val path = project.buildDir.toPath().resolve("${getProvider().name.get()}-work").toAbsolutePath().toString()
        File(path).mkdirs()
        return path
    }

    fun getProfile(): OperatorProfile {
        return when (productName) {
            ProductName.DEPLOY -> DeployExtensionUtil.getExtension(project).clusterProfiles.operator()
            ProductName.RELEASE -> ReleaseExtensionUtil.getExtension(project).clusterProfiles.operator()
        }
    }

    fun updateOperatorApplications() {
        if (getProvider().operatorPackageVersion.isPresent) {
            project.logger.lifecycle("Updating operator's applications")

            val file = File(getProviderHomeDir(), OPERATOR_APPS_REL_PATH)
            val pairs = mutableMapOf<String, Any>("spec[0].children[0].name" to getProvider().operatorPackageVersion.get())
            YamlFileUtil.overlayFile(file, pairs)
        }
    }

    fun updateOperatorDeployment() {
        if (getProvider().operatorPackageVersion.isPresent) {
            project.logger.lifecycle("Updating operator's deployment")

            val file = File(getProviderHomeDir(), OPERATOR_PACKAGE_REL_PATH)
            val pairs =
                mutableMapOf<String, Any>(
                    "spec.package" to "Applications/${getPrefixName()}-operator-app/${getProvider().operatorPackageVersion.get()}"
                )
            YamlFileUtil.overlayFile(file, pairs)
        }
    }

    fun updateOperatorDeploymentCr() {
        if (getProvider().operatorPackageVersion.isPresent) {
            project.logger.lifecycle("Updating operator's deployment CR")

            val file = File(getProviderHomeDir(), OPERATOR_CR_PACKAGE_REL_PATH)
            val pairs =
                mutableMapOf<String, Any>("spec.package" to "Applications/${getPrefixName()}-cr/${getProvider().operatorPackageVersion.get()}")
            YamlFileUtil.overlayFile(file, pairs)
        }
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
                "spec.oidc.enabled" to false,
                "spec.rabbitmq.persistence.storageClass" to getMqStorageClass(),
                "spec.rabbitmq.image.debug" to true,
                "spec.rabbitmq.persistence.size" to "1Gi",
                "spec.rabbitmq.replicaCount" to getProvider().rabbitmqReplicaCount.get(),
                "spec.rabbitmq.persistence.replicaCount" to 1,
                "spec.route.hosts" to arrayOf(getHost()),
                "spec.${getPrefixName()}License" to getLicense()
            )

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

    abstract fun getProvider(): Provider

    open fun getKubectlHelper(): KubeCtlHelper = KubeCtlHelper(project)

    open fun hasIngress(): Boolean = true

    open fun getWorkerPodName(position: Int) = "pod/dai-${getPrefixName()}-digitalai-${getName()}-worker-$position"

    open fun getMasterPodName(position: Int) =
        "pod/dai-${getPrefixName()}-digitalai-${getName()}-${getMasterPodNameSuffix(position)}"

    open fun getMasterPodNameSuffix(position: Int): String {
        return when (productName) {
            ProductName.DEPLOY -> "master-$position"
            ProductName.RELEASE -> "$position"
        }
    }

    open fun getPostgresPodName(position: Int) = "pod/dai-${getPrefixName()}-postgresql-$position"

    open fun getRabbitMqPodName(position: Int) = "pod/dai-${getPrefixName()}-rabbitmq-$position"

    fun getPrefixName(): String {
        return when (productName) {
            ProductName.DEPLOY -> "xld"
            ProductName.RELEASE -> "xlr"
        }
    }

    fun getTemplate(relativePath: String, targetFilename: String? = null): File {
        val file = File(relativePath)
        val fileStream = {}::class.java.classLoader.getResourceAsStream(relativePath)
        val resultComposeFilePath = Paths.get(getProviderWorkDir(), targetFilename ?: file.name)
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

    fun cleanUpCluster(waiting: Duration) {

        val resourcesList1 = arrayOf(
            "crd",
            "all",
            "roles",
            "roleBinding",
            "clusterRoles",
            "clusterRoleBinding",
            "ing",
            "ingressclass",
            "pvc"
        )
        // repeat delete of following resources to be sure that all is clean
        val resourcesList2 = arrayOf(
            "service",
            "crd"
        )

        runBlocking {
            for (iteration in 1..3) {
                project.logger.lifecycle("Clean up cluster resources iteration $iteration")

                val deleteResourcesJob = launch {
                    withTimeout(waiting.toMillis()) {
                        runInterruptible(Dispatchers.IO) {
                            deleteAllResources(resourcesList1, resourcesList2)
                        }
                    }
                }
                if (waitDeleteAllResources(deleteResourcesJob, iteration, waiting, resourcesList1, resourcesList2)) {
                    break
                }
            }
        }

        project.logger.lifecycle("Clean up cluster resources finished")
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

    private fun deleteAllResources(resourcesList1: Array<String>, resourcesList2: Array<String>) {
        deleteResources(resourcesList1)

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
        deleteResources(resourcesList2)
    }

    private suspend fun waitDeleteAllResources(
        deleteResourcesJob: Job, iteration: Int, waiting: Duration, resourcesList1: Array<String>, resourcesList2: Array<String>) : Boolean {
        val repeat = 10
        for (i in 1..repeat) {
            project.logger.lifecycle("Waiting cleanup $i")
            if (deleteResourcesJob.isActive) {
                delay(waiting.toMillis() / repeat)
            } else {
                break
            }
        }

        val existingResourcesFromList1 = getResources(resourcesList1)
        val existingResourcesFromList2 = getResources(resourcesList2)
        val hasResources = existingResourcesFromList1.isNotBlank() || existingResourcesFromList2.isNotBlank()
        return if (hasResources) {
            project.logger.lifecycle("Has more resources, cancelling in iteration $iteration: \n $existingResourcesFromList1 $existingResourcesFromList2")
            deleteResourcesJob.cancel()
            false
        } else {
            project.logger.lifecycle("Clean up cluster resources finished in iteration $iteration")
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