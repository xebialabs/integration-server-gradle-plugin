package ai.digital.integration.server.deploy.internals.cluster.operator

import ai.digital.integration.server.common.constant.OperatorProviderName
import ai.digital.integration.server.common.domain.InfrastructureInfo
import ai.digital.integration.server.common.domain.profiles.OperatorProfile
import ai.digital.integration.server.common.domain.providers.operator.Provider
import ai.digital.integration.server.common.util.*
import ai.digital.integration.server.deploy.internals.CliUtil
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.internals.WorkerUtil
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import org.gradle.api.Project
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

const val OPERATOR_FOLDER_NAME: String = "xl-deploy-kubernetes-operator"

const val CONTROLLER_MANAGER_REL_PATH = "digitalai-deploy/kubernetes/template/deployment.yaml"

const val OPERATOR_APPS_REL_PATH = "digitalai-deploy/applications.yaml"

const val OPERATOR_INFRASTRUCTURE_PATH = "digitalai-deploy/infrastructure.yaml"

const val OPERATOR_CR_PACKAGE_REL_PATH = "digitalai-deploy/deployment-cr.yaml"

const val OPERATOR_CR_VALUES_REL_PATH = "digitalai-deploy/kubernetes/daideploy_cr.yaml"

const val OPERATOR_PACKAGE_REL_PATH = "digitalai-deploy/deployment.yaml"

const val XL_DIGITAL_AI_PATH = "digital-ai.yaml "

@Suppress("UnstableApiUsage")
abstract class OperatorHelper(val project: Project) {

    companion object {
        private const val operatorMetadataPath = "deploy/operator/operator-metadata.properties"

        fun getOperatorHelper(project: Project): OperatorHelper {
            return when (val providerName = DeployClusterUtil.getOperatorProvider(project)) {
                OperatorProviderName.AWS_EKS.providerName ->
                    AwsEksHelper(project)
                OperatorProviderName.AWS_OPENSHIFT.providerName ->
                    AwsOpenshiftHelper(project)
                OperatorProviderName.AZURE_AKS.providerName ->
                    AzureAksHelper(project)
                OperatorProviderName.GCP_GKE.providerName ->
                    GcpGkeHelper(project)
                OperatorProviderName.ON_PREMISE.providerName ->
                    OnPremHelper(project)
                OperatorProviderName.VMWARE_OPENSHIFT.providerName ->
                    VmwareOpenshiftHelper(project)
                else -> {
                    throw IllegalArgumentException("Provided operator provider name `$providerName` is not supported. Choose one of ${
                        OperatorProviderName.values().joinToString()
                    }")
                }
            }
        }
    }

    fun getOperatorHomeDir(): String =
            project.buildDir.toPath().resolve(OPERATOR_FOLDER_NAME).toAbsolutePath().toString()

    fun getProviderWorkDir(): String =
            project.buildDir.toPath().resolve("${getProvider().name.get()}-work").toAbsolutePath().toString()

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

    fun waitForDeployment() {
        val resources = arrayOf(
                "deployment.apps/xld-operator-controller-manager",
                "deployment.apps/dai-xld-nginx-ingress-controller",
                "deployment.apps/dai-xld-nginx-ingress-controller-default-backend"
        )
        resources.forEach { resource ->
            if (!KubeCtlUtil.wait(project, resource, "Available", getProfile().deploymentTimeoutSeconds.get())) {
                throw RuntimeException("Resource $resource  is not available")
            }
        }
    }

    fun waitForMasterPods() {
        val resources = List(getMasterCount()) { position ->
            "pod/dai-xld-digitalai-deploy-master-$position"
        }

        resources.forEach { resource ->
            if (!KubeCtlUtil.wait(project, resource, "Ready", getProfile().deploymentTimeoutSeconds.get())) {
                throw RuntimeException("Resource $resource is not ready")
            }
        }
    }

    fun waitForWorkerPods() {
        val resources = List(getWorkerCount()) { position ->
            "pod/dai-xld-digitalai-deploy-worker-$position"
        }
        resources.forEach { resource ->
            if (!KubeCtlUtil.wait(project, resource, "Ready", getProfile().deploymentTimeoutSeconds.get())) {
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
        val url = "http://${getFqdn()}/xl-deploy/deployit/metadata/type"
        val server = DeployServerUtil.getServer(project)
        WaitForBootUtil.byPort(project, "Deploy", url, null, server.pingRetrySleepTime, server.pingTotalTries)
    }

    fun undeployCis() {
        val fileStream = {}::class.java.classLoader.getResourceAsStream("operator/python/undeploy.py")
        val resultComposeFilePath = Paths.get(getProviderWorkDir(), "undeploy.py")
        fileStream?.let {
            FileUtil.copyFile(it, resultComposeFilePath)
        }
        try {
            CliUtil.executeScripts(project, listOf(resultComposeFilePath.toFile()), "undeploy.py", auxiliaryServer = true)
        } catch (e: RuntimeException) {
            project.logger.warn("Undeploy didn't run. Check if operator's deploy server is running on port 4516: ${e.message}")
        }
    }

    open fun getOperatorImage(): String {
        return getProvider().operatorImage.value("xebialabs/deploy-operator:1.2.0").get()
    }

    fun updateOperatorCrValues() {
        val file = File(getProviderHomeDir(), OPERATOR_CR_VALUES_REL_PATH)
        val pairs = mutableMapOf<String, Any>(
            "spec.ImageRepository" to DeployServerUtil.getServer(project).dockerImage!!,
            "spec.ImageTag" to DeployServerUtil.getServer(project).version!!,
            "spec.XldMasterCount" to getMasterCount(),
            "spec.XldWorkerCount" to getWorkerCount(),
            "spec.Persistence.XldMasterPvcSize" to "1Gi",
            "spec.Persistence.XldWorkerPvcSize" to "1Gi",
            "spec.KeystorePassphrase" to getProvider().keystorePassphrase,
            "spec.Persistence.StorageClass" to getStorageClass(),
            "spec.RepositoryKeystore" to getProvider().repositoryKeystore,
            "spec.postgresql.image.debug" to true,
            "spec.postgresql.persistence.size" to "5Gi",
            "spec.postgresql.persistence.storageClass" to getDbStorageClass(),
            "spec.rabbitmq.persistence.storageClass" to getStorageClass(),
            "spec.rabbitmq.image.debug" to true,
            "spec.rabbitmq.image.tag" to "3.9.8-debian-10-r6", // original one is slow and unstable
            "spec.rabbitmq.persistence.size" to "1Gi",
            "spec.rabbitmq.replicaCount" to 1,
            "spec.rabbitmq.persistence.replicaCount" to 1,
            "spec.route.hosts" to arrayOf(getHost()),
            "spec.xldLicense" to getLicense()
        )
        YamlFileUtil.overlayFile(file, pairs, minimizeQuotes = false)
    }

    private fun getLicense(): String {
        val deployitLicenseFile = File(DeployServerUtil.getConfDir(project), "deployit-license.lic")
        val content = Files.readString(deployitLicenseFile.toPath())
        return Base64.getEncoder().encodeToString(content.toByteArray())
    }

    open fun getMasterCount(): Int {
        return DeployServerUtil.getServers(project).size
    }

    open fun getWorkerCount(): Int {
        return WorkerUtil.getNumberOfWorkers(project)
    }

    open fun getStorageClass(): String {
        return getProvider().storageClass.value("standard").get()
    }

    open fun getDbStorageClass(): String {
        return getStorageClass()
    }

    open fun getFqdn(): String {
        return getProvider().host.orElse(getProvider().name).get()
    }

    open fun getContextRoot(): String {
        return ""
    }

    open fun getHost(): String {
        return getProvider().host.orElse(getProvider().name).get()
    }

    open fun getPort(): String {
        return "80"
    }

    open fun applyYamlFiles() {
        val xlDigitalAiPath = File(getProviderHomeDir(), XL_DIGITAL_AI_PATH)
        project.logger.lifecycle("Applying Digital AI Deploy platform on cluster ($xlDigitalAiPath)")
        XlCliUtil.download(getProfile().xlCliVersion.get(), File(getProviderHomeDir()))
        XlCliUtil.xlApply(project, xlDigitalAiPath, File(getProviderHomeDir()))
    }

    open fun updateInfrastructure(infraInfo: InfrastructureInfo) {

    }

    abstract fun getProviderHomeDir(): String

    abstract fun getProvider(): Provider
}
