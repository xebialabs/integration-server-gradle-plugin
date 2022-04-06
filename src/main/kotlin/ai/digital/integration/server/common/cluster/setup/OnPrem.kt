package ai.digital.integration.server.common.cluster.setup

import ai.digital.integration.server.common.cluster.Helper
import ai.digital.integration.server.common.cluster.operator.OperatorHelper
import ai.digital.integration.server.common.constant.ClusterProfileName
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.providers.OnPremiseProvider
import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import org.gradle.api.Project
import org.gradle.api.provider.Property

open class OnPrem(project: Project, productName: ProductName) : Helper(project, productName) {

    override fun getProvider(): OnPremiseProvider {
        val profileName = getProfileName()
        if (profileName == ClusterProfileName.OPERATOR.profileName) {
            return OperatorHelper.getOperatorHelper(project, productName).getProfile().onPremise
        } else {
            throw IllegalArgumentException("Provided profile name `$profileName` is not supported")
        }
    }

    fun launchCluster() {
        val onPremiseProvider: OnPremiseProvider = getProvider()
        val name = onPremiseProvider.name.get()
        val skipExisting = onPremiseProvider.skipExisting.get()
        val kubernetesVersion = onPremiseProvider.kubernetesVersion.get()
        val driver = onPremiseProvider.driver.get()

        validateMinikubeCli()

        createCluster(
            name,
            driver,
            onPremiseProvider.clusterNodeCpus,
            onPremiseProvider.clusterNodeMemory,
            kubernetesVersion,
            skipExisting
        )
        updateContext(name)
        updateEtcHosts(name)
    }

    private fun validateMinikubeCli() {
        val result = ProcessUtil.executeCommand(
            project,
            "minikube version", throwErrorOnFailure = false, logOutput = false
        )
        if (!result.contains("minikube version")) {
            throw RuntimeException("No minikube-cli \"minikube\" in the path. Please verify your installation")
        }
    }

    private fun createCluster(
        name: String,
        driver: String,
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
            ProcessUtil.executeCommand(
                project,
                "minikube start --driver=$driver --kubernetes-version \"$kubernetesVersion\" -p $clusterName $additions"
            )
            ProcessUtil.executeCommand(
                project,
                "minikube addons enable ingress -p $clusterName"
            )
            ProcessUtil.executeCommand(
                project,
                "minikube addons enable ingress-dns -p $clusterName"
            )
        }
    }

    private fun onPremClusterName(name: String): String {
        return name
    }

    private fun shouldSkipExisting(name: String, skipExisting: Boolean): Boolean {
        val clusterName = onPremClusterName(name)
        return if (skipExisting) {
            val profileListResult = ProcessUtil.executeCommand(
                project,
                "minikube profile list | grep $clusterName", throwErrorOnFailure = false, logOutput = false
            )
            if (profileListResult.contains(clusterName)) {
                val profileResult = ProcessUtil.executeCommand(
                    project,
                    "minikube profile", throwErrorOnFailure = false, logOutput = false
                )
                if (!profileResult.contains(clusterName)) {
                    ProcessUtil.executeCommand(
                        project,
                        "minikube profile $clusterName", logOutput = false
                    )
                }
                true
            } else {
                false
            }
        } else {
            false
        }
    }

    private fun updateContext(name: String) {
        val clusterName = onPremClusterName(name)
        ProcessUtil.executeCommand(
            project,
            "minikube update-context -p $clusterName", throwErrorOnFailure = false
        )
    }

    fun updateEtcHosts(name: String, fqdn: String = getFqdn()) {
        val infoScriptPath = getTemplate("operator/on-perm/info_etc_hosts.sh")
        val scriptPath = getTemplate("operator/on-perm/update_etc_hosts.sh")

        ProcessUtil.executeCommand(
            project,
            "chmod 755 \"${infoScriptPath.absolutePath}\""
        )
        ProcessUtil.executeCommand(
            project,
            "chmod 755 \"${scriptPath.absolutePath}\""
        )
        ProcessUtil.executeCommand(
            project,
            "\"${infoScriptPath.absolutePath}\"", throwErrorOnFailure = false
        )
        ProcessUtil.executeCommand(
            project,
            "sudo \"${scriptPath.absolutePath}\" ${getMinikubeIp(name)} \"${fqdn}\"", throwErrorOnFailure = false
        )
    }

    private fun getMinikubeIp(name: String): String? {
        val clusterName = onPremClusterName(name)
        return try {
            val ip = ProcessUtil.executeCommand(
                project,
                "minikube -p $clusterName ip", logOutput = false
            )
            project.logger.lifecycle("Get cluster IP for {}: {}", clusterName, ip)
            ip
        } catch (e: RuntimeException) {
            null
        }
    }

    fun destroyClusterOnShutdown() {
        if (getProvider().destroyClusterOnShutdown.get()) {
            deleteCluster()
            project.logger.lifecycle("Current cluster context is being deleted")
            getKubectlHelper().deleteCurrentContext()
        }
    }

    private fun deleteCluster() {
        val onPremiseProvider = getProvider()
        val name = onPremiseProvider.name.get()
        val clusterName = onPremClusterName(name)
        project.logger.lifecycle("Minikube cluster is being deleted {} ", clusterName)
        ProcessUtil.executeCommand(
            project,
            "minikube delete -p $clusterName", throwErrorOnFailure = false
        )
    }

    override fun getFqdn(): String {
        return "${getHost()}.digitalai-testing.com"
    }


}