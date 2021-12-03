package ai.digital.integration.server.deploy.internals.cluster.operator

import ai.digital.integration.server.common.domain.InfrastructureInfo
import ai.digital.integration.server.common.domain.providers.operator.AzureAksProvider
import ai.digital.integration.server.common.util.*
import org.gradle.api.Project
import org.gradle.api.provider.Property
import java.io.File
import java.nio.file.Paths

open class AzureAksHelper(project: Project) : OperatorHelper(project) {

    fun launchCluster() {
        val azureAksProvider: AzureAksProvider = getProvider()
        val name = azureAksProvider.name.get()
        val skipExisting = azureAksProvider.skipExisting.get()
        val location = azureAksProvider.location.get()

        validateAzCli()
        loginAzCli(azureAksProvider.azUsername.orNull, azureAksProvider.azPassword.orNull)

        createResourceGroup(name, location, skipExisting)
        createCluster(name, azureAksProvider.clusterNodeCount, azureAksProvider.clusterNodeVmSize, azureAksProvider.kubernetesVersion, skipExisting)
        connectToCluster(name)
        val kubeContextInfo = KubeCtlUtil.getCurrentContextInfo(project)
        createStorageClass(azureAksProvider.storageClass.getOrElse(name))

        updateControllerManager()
        updateOperatorDeployment()
        updateOperatorDeploymentCr()
        updateInfrastructure(kubeContextInfo)
        updateOperatorCrValues()
        updateCrValues()

        applyYamlFiles()
        waitForDeployment()
        waitForMasterPods()
        waitForWorkerPods()

        waitForBoot(getFqdn(aksClusterName(name), location))
    }

    fun shutdownCluster() {
        val azureAksProvider: AzureAksProvider = getProvider()
        val name = azureAksProvider.name.get()

        val groupName = resourceGroupName(name)
        val location = azureAksProvider.location.get()
        val clusterName = aksClusterName(name)

        project.logger.lifecycle("Undeploy operator")
        undeployCis()

        project.logger.lifecycle("Delete all PVCs")
        KubeCtlUtil.deleteAllPvcs(project)

        project.logger.lifecycle("Delete resource group {} and AKS cluster {} ", groupName, clusterName)
        deleteResourceGroup(groupName, location)

        project.logger.lifecycle("Delete current context")
        KubeCtlUtil.deleteCurrentContext(project)
        logoutAzCli(azureAksProvider.azUsername.orNull, azureAksProvider.azPassword.orNull)
    }

    override fun updateInfrastructure(infraInfo: InfrastructureInfo) {
        val file = File(getProviderHomeDir(), OPERATOR_INFRASTRUCTURE_PATH)
        val pairs = mutableMapOf<String, Any>(
                "spec[0].children[0].apiServerURL" to infraInfo.apiServerURL!!,
                "spec[0].children[0].caCert" to infraInfo.caCert!!,
                "spec[0].children[0].tlsCert" to infraInfo.tlsCert!!,
                "spec[0].children[0].tlsPrivateKey" to infraInfo.tlsPrivateKey!!
        )
        YamlFileUtil.overlayFile(file, pairs)
    }

    override fun getProviderHomeDir(): String {
        return "${getOperatorHomeDir()}/deploy-operator-azure-aks"
    }

    override fun getProvider(): AzureAksProvider {
        return getProfile().azureAks
    }

    override fun getStorageClass(): String {
        return fileStorageClassName(getProvider().storageClass.getOrElse(getProvider().name.get()))
    }

    override fun getDbStorageClass(): String {
        return diskStorageClassName(getProvider().storageClass.getOrElse(getProvider().name.get()))
    }

    private fun getFqdn(cluster: String, location: String): String {
        return "${cluster}.${location}.cloudapp.azure.com"
    }

    private fun validateAzCli() {
        val result = ProcessUtil.executeCommand(project,
                "az -v", throwErrorOnFailure = false, logOutput = false)
        if (!result.contains("azure-cli")) {
            throw RuntimeException("No azure-cli \"az\" in the path. Please verify your installation")
        }
    }

    private fun loginAzCli(username: String?, password: String?) {
        if (username != null && password != null) {
            project.logger.lifecycle("Login user")
            ProcessUtil.executeCommand(project,
                    "az login -u $username -p $password", throwErrorOnFailure = false, logOutput = false)
        }
    }

    private fun logoutAzCli(username: String?, password: String?) {
        if (username != null && password != null) {
            project.logger.lifecycle("Logout user")
            ProcessUtil.executeCommand(project,
                    "az logout", throwErrorOnFailure = false)
        }
    }
    private fun createStorageClass(name: String) {
        val fileStorageClassName = fileStorageClassName(name)
        val diskStorageClassName = diskStorageClassName(name)

        if (!KubeCtlUtil.hasStorageClass(project, fileStorageClassName)) {
            project.logger.lifecycle("Create storage class: {}", fileStorageClassName)
            val azureFileScTemplateFile = getTemplate("operator/azure-aks/azure-file-sc.yaml")
            val azureFileScTemplate = azureFileScTemplateFile.readText(Charsets.UTF_8)
                    .replace("{{NAME}}", fileStorageClassName)
            azureFileScTemplateFile.writeText(azureFileScTemplate)
            KubeCtlUtil.apply(project, azureFileScTemplateFile)
        } else {
            project.logger.lifecycle("Skipping creation of the existing storage class: {}", fileStorageClassName)
        }

        if (!KubeCtlUtil.hasStorageClass(project, diskStorageClassName)) {
            project.logger.lifecycle("Create storage class: {}", diskStorageClassName)
            val azureDiskScTemplateFile = getTemplate("operator/azure-aks/azure-disk-sc.yaml")
            val azureDiskScTemplate = azureDiskScTemplateFile.readText(Charsets.UTF_8)
                    .replace("{{NAME}}", diskStorageClassName)
            azureDiskScTemplateFile.writeText(azureDiskScTemplate)
            KubeCtlUtil.apply(project, azureDiskScTemplateFile)
        } else {
            project.logger.lifecycle("Skipping creation of the existing storage class: {}", diskStorageClassName)
        }

        KubeCtlUtil.setDefaultStorageClass(project, "default", fileStorageClassName)
    }

    private fun existsResourceGroup(groupName: String, location: String): Boolean {
        val result = ProcessUtil.executeCommand(project,
                "az group list --query \"[?location=='$location']\" --output tsv | grep $groupName", throwErrorOnFailure = false, logOutput = false)
        return result.contains(groupName)
    }

    private fun createResourceGroup(name: String, location: String, skipExisting: Boolean) {
        val groupName = resourceGroupName(name)
        var shouldSkipExisting = false
        if (skipExisting) {
            if (existsResourceGroup(groupName, location)) {
                shouldSkipExisting = true
            }
        }
        if (shouldSkipExisting) {
            project.logger.lifecycle("Skipping creation of the existing resource group: {}", groupName)
        } else {
            project.logger.lifecycle("Create resource group: {}", groupName)
            ProcessUtil.executeCommand(project,
                    "az group create --name $groupName --location $location")
        }
    }

    private fun deleteResourceGroup(groupName: String, location: String) {
        if (existsResourceGroup(groupName, location)) {
            project.logger.lifecycle("Create resource group: {}", groupName)
            ProcessUtil.executeCommand(project,
                    "az group delete --name $groupName --yes")
        } else {
            project.logger.lifecycle("Skipping delete of the resource group: {}", groupName)
        }
    }

    private fun createCluster(name: String, clusterNodeCount: Property<Int>, clusterNodeVmSize: Property<String>, kubernetesVersion: Property<String>, skipExisting: Boolean) {
        val groupName = resourceGroupName(name)
        val clusterName = aksClusterName(name)
        var shouldSkipExisting = false
        if (skipExisting) {
            val result = ProcessUtil.executeCommand(project,
                    "az aks list --output tsv | grep $clusterName", throwErrorOnFailure = false, logOutput = false)
            if (result.contains(clusterName)) {
                shouldSkipExisting = true
            }
        }
        if (shouldSkipExisting) {
            project.logger.lifecycle("Skipping creation of the existing AKS cluster: {}", clusterName)
        } else {
            project.logger.lifecycle("Create AKS cluster: {}", clusterName)
            var additions = ""
            if (clusterNodeVmSize.isPresent) {
                additions += " --node-vm-size \"${clusterNodeVmSize.get()}\""
            }
            if (kubernetesVersion.isPresent) {
                additions += " --kubernetes-version \"${kubernetesVersion.get()}\""
            }
            ProcessUtil.executeCommand(project,
                    "az aks create --resource-group $groupName --name $clusterName --node-count ${clusterNodeCount.getOrElse(2)} " +
                            "--generate-ssh-keys --enable-addons monitoring $additions")
        }
    }

    private fun connectToCluster(name: String) {
        ProcessUtil.executeCommand(project,
                "az aks get-credentials --resource-group ${resourceGroupName(name)} --name ${aksClusterName(name)} --overwrite-existing")
    }

    private fun updateCrValues() {
        val azureAksProvider: AzureAksProvider = getProvider()

        val name = azureAksProvider.name.get()
        val cluster = aksClusterName(name)
        val location = azureAksProvider.location.get()

        val file = File(getProviderHomeDir(), OPERATOR_CR_VALUES_REL_PATH)
        val pairs = mutableMapOf(
                "spec.nginx-ingress-controller.service.annotations" to mapOf("service.beta.kubernetes.io/azure-dns-label-name" to cluster),
                "spec.ingress.hosts" to arrayOf(getFqdn(cluster, location))
        )
        YamlFileUtil.overlayFile(file, pairs)
    }

    private fun resourceGroupName(name: String): String {
        return "${name}-group"
    }

    private fun aksClusterName(name: String): String {
        return name
    }

    private fun getTemplate(relativePath: String): File {
        val file = File(relativePath)
        val fileStream = {}::class.java.classLoader.getResourceAsStream(relativePath)
        val resultComposeFilePath = Paths.get(getProviderWorkDir(), file.name)
        fileStream?.let {
            FileUtil.copyFile(it, resultComposeFilePath)
        }
        return resultComposeFilePath.toFile()
    }

    private fun diskStorageClassName(name: String): String {
        return "${name}-disk-storage-class"
    }

    private fun fileStorageClassName(name: String): String {
        return "${name}-file-storage-class"
    }
}
