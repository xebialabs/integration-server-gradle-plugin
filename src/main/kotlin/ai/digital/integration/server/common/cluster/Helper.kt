package ai.digital.integration.server.common.cluster

import ai.digital.integration.server.common.constant.ClusterProfileName
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.constant.ServerConstants
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.domain.profiles.IngressType
import ai.digital.integration.server.common.domain.profiles.OperatorHelmProfile
import ai.digital.integration.server.common.domain.providers.Provider
import ai.digital.integration.server.common.util.*
import ai.digital.integration.server.deploy.domain.Worker
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.internals.WorkerUtil
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import ai.digital.integration.server.release.internals.ReleaseExtensionUtil
import ai.digital.integration.server.release.tasks.cluster.ReleaseClusterUtil
import ai.digital.integration.server.release.util.ReleaseServerUtil
import org.gradle.api.Project
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

abstract class Helper(val project: Project, val productName: ProductName) {

    abstract fun getProvider(): Provider

    open fun getKubectlHelper(): KubeCtlHelper = KubeCtlHelper(project, getNamespace())

    open fun getMasterCount(): Int {
        return when (productName) {
            ProductName.DEPLOY -> DeployServerUtil.getServers(project).size
            ProductName.RELEASE -> ReleaseExtensionUtil.getExtension(project).servers.size
        }
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

    open fun getHost(): String {
        return getProvider().host.getOrElse(getProvider().name.get())
    }

    open fun getPort(): String {
        return "80"
    }

    open fun hasIngress(): Boolean = true

    open fun getWorkerPodName(position: Int) = "pod/${getCrName()}-digitalai-${getName()}-worker-$position"

    open fun getMasterPodName(position: Int) =
        "pod/${getCrName()}-digitalai-${getName()}-${getMasterPodNameSuffix(position)}"

    open fun getPostgresPodName(position: Int) = "pod/${getCrName()}-postgresql-$position"

    open fun getRabbitMqPodName(position: Int) = "pod/${getCrName()}-rabbitmq-$position"

    open fun getNamespace(): String? = getProfile().namespace.orNull

    open fun getCrName(): String {
        val operatorNamespace = getNamespace()?.let { "-$it" } ?: ""
        return "dai-${getPrefixName()}$operatorNamespace"
    }

    open fun getMasterPodNameSuffix(position: Int): String {
        return when (productName) {
            ProductName.DEPLOY -> "master-$position"
            ProductName.RELEASE -> "$position"
        }
    }

    fun getContextRootPath(file: File, pathKey: String): String {
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

    fun getProfileName(): String {
        return when (productName) {
            ProductName.DEPLOY -> DeployClusterUtil.getProfile(project)
            ProductName.RELEASE -> ReleaseClusterUtil.getProfile(project)
        }
    }

    fun getProfile(): OperatorHelmProfile {
        when (val profileName = getProfileName()) {
            ClusterProfileName.OPERATOR.profileName -> {
                return when (productName) {
                    ProductName.DEPLOY -> DeployExtensionUtil.getExtension(project).clusterProfiles.operator()
                    ProductName.RELEASE -> ReleaseExtensionUtil.getExtension(project).clusterProfiles.operator()
                }
            }
            ClusterProfileName.HELM.profileName -> {
                return when (productName) {
                    ProductName.DEPLOY -> DeployExtensionUtil.getExtension(project).clusterProfiles.helm()
                    ProductName.RELEASE -> ReleaseExtensionUtil.getExtension(project).clusterProfiles.helm()
                }
            }
            else -> {
                throw IllegalArgumentException("Provided profile name `$profileName` is not supported")
            }
        }
    }

    fun getProviderWorkDir(): String {
        val path = project.layout.buildDirectory.get().asFile.toPath()
            .resolve("${getProvider().name.get()}-work")
            .toAbsolutePath()
            .toString()
        File(path).mkdirs()
        return path
    }

    fun getServerImageRepository(): String {
        return getServer().dockerImage!!
    }

    fun getCentralConfigImageRepository(): String {
        return getServer().centralConfigDockerImage!!
    }

    fun getDeployWorkerImageRepository(): String {
        return getDeployWorker().dockerImage!!
    }

    fun getServerVersion(): String {
        return getServer().version!!
    }

    fun getServer(): Server {
        return when (productName) {
            ProductName.DEPLOY -> DeployServerUtil.getServer(project)
            ProductName.RELEASE -> ReleaseServerUtil.getServer(project)
        }
    }

    fun getDeployWorker(): Worker {
        return WorkerUtil.getWorkers(project)[0]
    }

    fun getDbConnectionCount(): String {
        val defaultMaxDbConnections = when (productName) {
            ProductName.DEPLOY ->
                ServerConstants.DEPLOY_DB_CONNECTION_NUMBER * (getMasterCount() + getDeployWorkerCount())
            ProductName.RELEASE ->
                ServerConstants.RELEASE_DB_CONNECTION_NUMBER * getMasterCount()
        }
        return getProvider().maxDbConnections.getOrElse(defaultMaxDbConnections).toString()
    }

    fun getLicense(): String {
        val licenseFileName = when (productName) {
            ProductName.DEPLOY -> "deployit-license.lic"
            ProductName.RELEASE -> "xl-release-license.lic"
        }
        val licenseFile = File(getConfigDir(), licenseFileName)
        val content = Files.readString(licenseFile.toPath())
        return Base64.getEncoder().encodeToString(content.toByteArray())
    }

    fun getName(): String {
        return productName.toString().lowercase()
    }

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

    fun clusterMetadata(metaDataPath: String, contextRoot: String) {
        val path = IntegrationServerUtil.getRelativePathInIntegrationServerDist(project, metaDataPath)
        path.parent.toFile().mkdirs()
        val props = Properties()
        props["cluster.port"] = getPort()
        props["cluster.context-root"] = contextRoot
        props["cluster.host"] = getHost()
        props["cluster.fqdn"] = getFqdn()
        PropertiesUtil.writePropertiesFile(path.toFile(), props)
    }

    /**
     * AWSOpenshift
     */
    fun exec(command: String): String {
        /* val workDir = File(getProviderHomeDir())
         if (!workDir.exists()) {
             workDir.mkdirs()
         }*/
        return ProcessUtil.executeCommand(command)
    }

    fun waitForDeployment(ingressType : String, deploymentTimeoutSeconds: Int, skipOperator: Boolean = false) {
        val resources = if (hasIngress()) {
            when (IngressType.valueOf(ingressType)) {
                IngressType.NGINX ->
                    arrayOf(
                            "deployment.apps/${getCrName()}-nginx-ingress-controller",
                            "deployment.apps/${getCrName()}-nginx-ingress-controller-default-backend"
                    )
                IngressType.HAPROXY ->
                    arrayOf("deployment.apps/${getCrName()}-haproxy-ingress")
            }
        } else
            arrayOf()

        (resources + "deployment.apps/${getPrefixName()}-operator-controller-manager").forEach { resource ->
            if (!skipOperator && !getKubectlHelper().wait(resource, "Available", deploymentTimeoutSeconds)) {
                throw RuntimeException("Resource $resource  is not available")
            }
        }
    }

    fun waitForMasterPods(deploymentTimeoutSeconds: Int) {
        val resources = List(getMasterCount()) { position -> getMasterPodName(position) }

        resources.forEach { resource ->
            if (!getKubectlHelper().wait(resource, "Ready", deploymentTimeoutSeconds)) {
                throw RuntimeException("Resource $resource is not ready")
            }
        }
    }

    fun waitForWorkerPods(deploymentTimeoutSeconds: Int) {
        val resources = List(getDeployWorkerCount()) { position -> getWorkerPodName(position) }
        resources.forEach { resource ->
            if (!getKubectlHelper().wait(resource, "Ready", deploymentTimeoutSeconds)) {
                throw RuntimeException("Resource $resource is not ready")
            }
        }
    }
    private fun getConfigDir(): File {
        return when (productName) {
            ProductName.DEPLOY -> DeployServerUtil.getConfDir(project)
            ProductName.RELEASE -> ReleaseServerUtil.getConfDir(project)
        }
    }

    fun waitForBoot(pContextRoot : String, fqdn: String) {
        val contextRoot = when (pContextRoot == "/") {
            true -> ""
            false -> pContextRoot
        }

        val url = when (productName) {
            ProductName.DEPLOY -> "http://${fqdn}${contextRoot}/deployit/metadata/type"
            ProductName.RELEASE -> "http://${fqdn}${contextRoot}/api/extension/metadata"
        }
        val server = ServerUtil(project, productName).getServer()
        WaitForBootUtil.byPort(project, getName(), url, null, server.pingRetrySleepTime, server.pingTotalTries)
    }

}
