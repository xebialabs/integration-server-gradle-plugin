package ai.digital.integration.server.common.cluster

import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.providers.Provider
import ai.digital.integration.server.common.util.FileUtil
import ai.digital.integration.server.common.util.KubeCtlHelper
import ai.digital.integration.server.common.util.ProcessUtil
import org.gradle.api.Project
import java.io.File
import java.nio.file.Paths

abstract class Helper(val project: Project, val productName: ProductName) {

    abstract fun getProvider(): Provider

    fun getProviderWorkDir(): String {
        val path = project.buildDir.toPath().resolve("${getProvider().name.get()}-work").toAbsolutePath().toString()
        File(path).mkdirs()
        return path
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

    open fun getStorageClass(): String {
        return getProvider().storageClass.getOrElse("standard")
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

    open fun getKubectlHelper(): KubeCtlHelper = KubeCtlHelper(project)

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

}