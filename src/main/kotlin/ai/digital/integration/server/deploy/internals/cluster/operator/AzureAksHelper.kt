package ai.digital.integration.server.deploy.internals.cluster.operator

import ai.digital.integration.server.common.domain.InfrastructureInfo
import ai.digital.integration.server.common.domain.providers.operator.AzureAksProvider
import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.common.util.YamlFileUtil
import org.gradle.api.Project
import org.gradle.api.provider.Property
import java.io.File

open class AzureAksHelper(project: Project) : OperatorHelper(project) {

    fun launchCluster() {
        val azureAksProvider: AzureAksProvider = getProvider()
        val name = azureAksProvider.name.get()
        val skipExisting = azureAksProvider.skipExisting.get()
        val location = azureAksProvider.location.get()

        validateAzCli()
        loginAzCli(azureAksProvider.getAzUsername(), azureAksProvider.getAzPassword())

        createResourceGroup(name, location, skipExisting)
        createCluster(name,
            azureAksProvider.clusterNodeCount,
            azureAksProvider.clusterNodeVmSize,
            azureAksProvider.kubernetesVersion,
            skipExisting)
        connectToCluster(name)
        val kubeContextInfo = getCurrentContextInfo()
        createStorageClass(azureAksProvider.storageClass.getOrElse(name))

        updateControllerManager()
        updateOperatorDeployment()
        updateOperatorDeploymentCr()
        updateInfrastructure(kubeContextInfo)
        updateOperatorCrValues()
        updateCrValues()

        applyYamlFiles()
        turnOnLogging()
        waitForDeployment()
        waitForMasterPods()
        waitForWorkerPods()

        createClusterMetadata()
        waitForBoot()
        turnOffLogging()
    }

    fun shutdownCluster() {
        val azureAksProvider: AzureAksProvider = getProvider()
        val name = azureAksProvider.name.get()

        val groupName = resourceGroupName(name)
        val location = azureAksProvider.location.get()

        val existsResourceGroup = existsResourceGroup(groupName, location)
        if (existsResourceGroup) {
            undeployCluster()
        }

        if (azureAksProvider.destroyClusterOnShutdown.get()) {
            if (existsResourceGroup) {
                deleteResourceGroup(name, groupName, location)
            }

            getKubectlHelper().deleteCurrentContext()
            logoutAzCli(azureAksProvider.getAzUsername(), azureAksProvider.getAzPassword())
        }
    }

    private fun updateInfrastructure(infraInfo: InfrastructureInfo) {
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

    override fun getFqdn(): String {
        val azureAksProvider: AzureAksProvider = getProvider()
        val location = azureAksProvider.location.get()
        return "${getHost()}.${location}.cloudapp.azure.com"
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

    private fun createStorageClassFromFile(storageClassName: String, filePath: String) {
        if (!getKubectlHelper().hasStorageClass(storageClassName)) {
            project.logger.lifecycle("Create storage class: {}", storageClassName)
            val azureFileScTemplateFile = getTemplate(filePath)
            val azureFileScTemplate = azureFileScTemplateFile.readText(Charsets.UTF_8)
                .replace("{{NAME}}", storageClassName)
            azureFileScTemplateFile.writeText(azureFileScTemplate)
            getKubectlHelper().applyFile(azureFileScTemplateFile)
        } else {
            project.logger.lifecycle("Skipping creation of the existing storage class: {}", storageClassName)
        }
    }

    private fun createStorageClass(name: String) {
        val fileStorageClassName = fileStorageClassName(name)
        createStorageClassFromFile(fileStorageClassName, "operator/azure-aks/azure-file-sc.yaml")
        val diskStorageClassName = diskStorageClassName(name)
        createStorageClassFromFile(diskStorageClassName, "operator/azure-aks/azure-disk-sc.yaml")

        getKubectlHelper().setDefaultStorageClass(fileStorageClassName)
    }

    private fun existsResourceGroup(groupName: String, location: String): Boolean {
        val result = ProcessUtil.executeCommand(project,
            "az group list --query \"[?location=='$location']\" --output tsv | grep $groupName",
            throwErrorOnFailure = false,
            logOutput = false)
        return result.contains(groupName)
    }

    private fun createResourceGroup(name: String, location: String, skipExisting: Boolean) {
        val groupName = resourceGroupName(name)
        val shouldSkipExisting = if (skipExisting) {
            existsResourceGroup(groupName, location)
        } else {
            false
        }
        if (shouldSkipExisting) {
            project.logger.lifecycle("Skipping creation of the existing resource group: {}", groupName)
        } else {
            project.logger.lifecycle("Create resource group: {}", groupName)
            ProcessUtil.executeCommand(project,
                "az group create --name $groupName --location $location")
        }
    }

    private fun deleteResourceGroup(name: String, groupName: String, location: String) {
        val clusterName = aksClusterName(name)
        project.logger.lifecycle("Delete resource group {} and AKS cluster {} ", groupName, clusterName)
        if (existsResourceGroup(groupName, location)) {
            project.logger.lifecycle("Delete resource group: {}", groupName)
            ProcessUtil.executeCommand(project,
                "az group delete --name $groupName --yes")
        } else {
            project.logger.lifecycle("Skipping delete of the resource group: {}", groupName)
        }
    }

    private fun createCluster(
        name: String,
        clusterNodeCount: Property<Int>,
        clusterNodeVmSize: Property<String>,
        kubernetesVersion: Property<String>,
        skipExisting: Boolean
    ) {
        val groupName = resourceGroupName(name)
        val clusterName = aksClusterName(name)
        val shouldSkipExisting = if (skipExisting) {
            val result = ProcessUtil.executeCommand(project,
                "az aks list --output tsv | grep $clusterName", throwErrorOnFailure = false, logOutput = false)
            result.contains(clusterName)
        } else {
            false
        }
        if (shouldSkipExisting) {
            project.logger.lifecycle("Skipping creation of the existing AKS cluster: {}", clusterName)
        } else {
            project.logger.lifecycle("Create AKS cluster: {}", clusterName)
            val additions = clusterNodeVmSize.map { " --node-vm-size \"$it\"" }.getOrElse("") +
                    kubernetesVersion.map { " --kubernetes-version \"$it\"" }.getOrElse("")
            ProcessUtil.executeCommand(project,
                "az aks create --resource-group $groupName --name $clusterName --node-count ${
                    clusterNodeCount.getOrElse(2)
                } " +
                        "--generate-ssh-keys --enable-addons monitoring $additions")
        }
    }

    private fun connectToCluster(name: String) {
        ProcessUtil.executeCommand(project,
            "az aks get-credentials --resource-group ${resourceGroupName(name)} --name ${aksClusterName(name)} --overwrite-existing")
    }

    private fun updateCrValues() {
        val file = File(getProviderHomeDir(), OPERATOR_CR_VALUES_REL_PATH)
        val pairs: MutableMap<String, Any> = mutableMapOf(
            "spec.nginx-ingress-controller.service.annotations" to mapOf("service.beta.kubernetes.io/azure-dns-label-name" to getHost()),
            "spec.ingress.hosts" to arrayOf(getFqdn())
        )
        YamlFileUtil.overlayFile(file, pairs, minimizeQuotes = false)
    }

    private fun resourceGroupName(name: String): String {
        return "${name}-group"
    }

    private fun aksClusterName(name: String): String {
        return name
    }

    private fun diskStorageClassName(name: String): String {
        return "${name}-disk-storage-class"
    }

    private fun fileStorageClassName(name: String): String {
        return "${name}-file-storage-class"
    }

    override fun getCurrentContextInfo() = getKubectlHelper().getCurrentContextInfo()
}
