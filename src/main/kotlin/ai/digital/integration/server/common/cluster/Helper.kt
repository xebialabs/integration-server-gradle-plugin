package ai.digital.integration.server.common.cluster

import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.constant.ServerConstants
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.domain.profiles.OperatorProfile
import ai.digital.integration.server.common.domain.profiles.Profile
import ai.digital.integration.server.common.domain.providers.Provider
import ai.digital.integration.server.common.util.FileUtil
import ai.digital.integration.server.common.util.KubeCtlHelper
import ai.digital.integration.server.common.util.ProcessUtil
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

    abstract fun getProfile(): Profile

    open fun getKubectlHelper(): KubeCtlHelper = KubeCtlHelper(project, null)

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

    fun getProfileName(): String {
        return when (productName) {
            ProductName.DEPLOY -> DeployClusterUtil.getProfile(project)
            ProductName.RELEASE -> ReleaseClusterUtil.getProfile(project)
        }
    }

    fun getProviderWorkDir(): String {
        val path = project.buildDir.toPath().resolve("${getProvider().name.get()}-work").toAbsolutePath().toString()
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
        return productName.toString().toLowerCase()
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

    private fun getConfigDir(): File {
        return when (productName) {
            ProductName.DEPLOY -> DeployServerUtil.getConfDir(project)
            ProductName.RELEASE -> ReleaseServerUtil.getConfDir(project)
        }
    }

}