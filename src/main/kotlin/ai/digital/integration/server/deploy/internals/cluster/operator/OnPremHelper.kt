package ai.digital.integration.server.deploy.internals.cluster.operator

import ai.digital.integration.server.common.domain.InfrastructureInfo
import ai.digital.integration.server.common.domain.providers.operator.OnPremiseProvider
import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.common.util.YamlFileUtil
import org.gradle.api.Project
import org.gradle.api.provider.Property
import java.io.File

open class OnPremHelper(project: Project) : OperatorHelper(project) {

    fun launchCluster() {
        val onPremiseProvider: OnPremiseProvider = getProvider()
        val name = onPremiseProvider.name.get()
        val skipExisting = onPremiseProvider.skipExisting.get()
        val kubernetesVersion = onPremiseProvider.kubernetesVersion.get()

        validateMinikubeCli()

        createCluster(name,
            onPremiseProvider.clusterNodeCpus,
            onPremiseProvider.clusterNodeMemory,
            kubernetesVersion,
            skipExisting)
        updateContext(name)
        val kubeContextInfo = getKubectlHelper().getCurrentContextInfo()

        updateControllerManager()
        updateOperatorDeployment()
        updateOperatorDeploymentCr()
        updateInfrastructure(kubeContextInfo)
        updateOperatorCrValues()
        updateCrValues()

        updateEtcHosts(name)

        applyYamlFiles()
        waitForDeployment()
        waitForMasterPods()
        waitForWorkerPods()

        createClusterMetadata()
        waitForBoot()
    }

    fun shutdownCluster() {
        val onPremiseProvider: OnPremiseProvider = getProvider()
        val name = onPremiseProvider.name.get()

        undeployCluster()

        deleteCluster(name)

        project.logger.lifecycle("Current cluster context is being deleted")
        getKubectlHelper().deleteCurrentContext()
    }

    override fun getProviderHomeDir(): String {
        return "${getOperatorHomeDir()}/deploy-operator-onprem"
    }

    override fun getProvider(): OnPremiseProvider {
        return getProfile().onPremise
    }

    fun updateInfrastructure(infraInfo: InfrastructureInfo) {
        val file = File(getProviderHomeDir(), OPERATOR_INFRASTRUCTURE_PATH)
        val pairs = mutableMapOf<String, Any>(
            "spec[0].children[0].apiServerURL" to infraInfo.apiServerURL!!,
            "spec[0].children[0].caCert" to infraInfo.caCert!!,
            "spec[0].children[0].tlsCert" to infraInfo.tlsCert!!,
            "spec[0].children[0].tlsPrivateKey" to infraInfo.tlsPrivateKey!!
        )
        YamlFileUtil.overlayFile(file, pairs)
    }

    override fun getFqdn(): String {
        return "${getHost()}.digitalai-testing.com"
    }

    private fun validateMinikubeCli() {
        val result = ProcessUtil.executeCommand(project,
            "minikube version", throwErrorOnFailure = false, logOutput = false)
        if (!result.contains("minikube version")) {
            throw RuntimeException("No minikube-cli \"minikube\" in the path. Please verify your installation")
        }
    }

    private fun shouldSkipExisting(name: String, skipExisting: Boolean): Boolean {
        val clusterName = onPremClusterName(name)
        return if (skipExisting) {
            val profileListResult = ProcessUtil.executeCommand(project,
                "minikube profile list | grep $clusterName", throwErrorOnFailure = false, logOutput = false)
            if (profileListResult.contains(clusterName)) {
                val profileResult = ProcessUtil.executeCommand(project,
                    "minikube profile", throwErrorOnFailure = false, logOutput = false)
                if (!profileResult.contains(clusterName)) {
                    ProcessUtil.executeCommand(project,
                        "minikube profile $clusterName", logOutput = false)
                }
                true
            } else {
                false
            }
        } else {
            false
        }
    }

    private fun createCluster(
        name: String,
        clusterNodeCpus: Property<Int>,
        clusterNodeMemory: Property<Int>,
        kubernetesVersion: String,
        skipExisting: Boolean
    ) {
        val clusterName = onPremClusterName(name)
        val shouldSkipExisting = shouldSkipExisting(name, skipExisting)

        if (shouldSkipExisting) {
            project.logger.lifecycle("Skipping creation of the existing minikube cluster: {}", clusterName)
        } else {
            project.logger.lifecycle("Create minikube cluster: {}", clusterName)
            val additions = clusterNodeCpus.map { " --cpus \"$it\"" }.getOrElse("") +
                    clusterNodeMemory.map { " --memory \"$it\"" }.getOrElse("")
            ProcessUtil.executeCommand(project,
                "minikube start --driver=virtualbox --kubernetes-version \"$kubernetesVersion\" -p $clusterName $additions")
            ProcessUtil.executeCommand(project,
                "minikube addons enable ingress -p $clusterName")
            ProcessUtil.executeCommand(project,
                "minikube addons enable ingress-dns -p $clusterName")
        }
    }

    private fun updateContext(name: String) {
        val clusterName = onPremClusterName(name)
        ProcessUtil.executeCommand(project,
            "minikube update-context -p $clusterName", throwErrorOnFailure = false)
    }

    private fun deleteCluster(name: String) {
        val clusterName = onPremClusterName(name)
        project.logger.lifecycle("Minikube cluster is being deleted {} ", clusterName)
        ProcessUtil.executeCommand(project,
            "minikube delete -p $clusterName", throwErrorOnFailure = false)
    }

    private fun updateEtcHosts(name: String) {
        val infoScriptPath = getTemplate("operator/on-perm/info_etc_hosts.sh")
        val scriptPath = getTemplate("operator/on-perm/update_etc_hosts.sh")

        ProcessUtil.executeCommand(project,
            "chmod 755 \"${infoScriptPath.absolutePath}\"")
        ProcessUtil.executeCommand(project,
            "chmod 755 \"${scriptPath.absolutePath}\"")
        ProcessUtil.executeCommand(project,
            "\"${infoScriptPath.absolutePath}\"", throwErrorOnFailure = false)
        ProcessUtil.executeCommand(project,
            "sudo \"${scriptPath.absolutePath}\" ${getMinikubeIp(name)} \"${getFqdn()}\"", throwErrorOnFailure = false)
    }

    private fun getMinikubeIp(name: String): String? {
        val clusterName = onPremClusterName(name)
        return try {
            val ip = ProcessUtil.executeCommand(project,
                "minikube -p $clusterName ip", logOutput = false)
            project.logger.lifecycle("Get cluster IP for {}: {}", clusterName, ip)
            ip
        } catch (e: RuntimeException) {
            null
        }
    }

    private fun onPremClusterName(name: String): String {
        return name
    }

    private fun updateCrValues() {
        val file = File(getProviderHomeDir(), OPERATOR_CR_VALUES_REL_PATH)
        val pairs: MutableMap<String, Any> = mutableMapOf(
            "spec.ingress.hosts" to listOf(getFqdn()),
            "spec.nginx-ingress-controller.defaultBackend.podSecurityContext" to mapOf("fsGroup" to 1001),
            "spec.nginx-ingress-controller.podSecurityContext" to mapOf("fsGroup" to 1001)
        )
        YamlFileUtil.overlayFile(file, pairs, minimizeQuotes = false)
    }
}
