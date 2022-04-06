package ai.digital.integration.server.common.cluster.setup

import ai.digital.integration.server.common.cluster.Helper
import ai.digital.integration.server.common.cluster.operator.OperatorHelper
import ai.digital.integration.server.common.constant.ClusterProfileName
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.providers.AzureAksProvider
import ai.digital.integration.server.common.util.ProcessUtil
import org.gradle.api.Project
import org.gradle.api.provider.Property

open class AzureAksHelper(project: Project, productName: ProductName) : Helper(project, productName) {

    override fun getProvider(): AzureAksProvider {
        val profileName = getProfileName()
        if (profileName == ClusterProfileName.OPERATOR.profileName) {
            return OperatorHelper.getOperatorHelper(project, productName).getProfile().azureAks
        } else {
            throw IllegalArgumentException("Provided profile name `$profileName` is not supported")
        }
    }



    fun launchCluster() {
        val azureAksProvider: AzureAksProvider = getProvider()
        val name = azureAksProvider.name.get()
        val skipExisting = azureAksProvider.skipExisting.get()
        val location = azureAksProvider.location.get()

        validateAzCli()
        loginAzCli(azureAksProvider.getAzUsername(), azureAksProvider.getAzPassword())

        createResourceGroup(name, location, skipExisting)
        createCluster(
            name,
            azureAksProvider.clusterNodeCount,
            azureAksProvider.clusterNodeVmSize,
            azureAksProvider.kubernetesVersion,
            skipExisting
        )
        connectToCluster(name)
        createStorageClass(resourceGroupName(name), azureAksProvider.storageClass.getOrElse(name))
    }

    private fun validateAzCli() {
        val result = ProcessUtil.executeCommand(
            project,
            "az -v", throwErrorOnFailure = false, logOutput = false
        )
        if (!result.contains("azure-cli")) {
            throw RuntimeException("No azure-cli \"az\" in the path. Please verify your installation")
        }
    }

    private fun loginAzCli(username: String?, password: String?) {
        if (username != null && password != null) {
            project.logger.lifecycle("Login user")
            ProcessUtil.executeCommand(
                project,
                "az login -u $username -p $password", throwErrorOnFailure = false, logOutput = false
            )
        }
    }

    private fun createStorageClass(resourceGroupName: String, name: String) {
        val fileStorageClassName = fileStorageClassName(name)
        createStorageClassFromFile(resourceGroupName, fileStorageClassName, "operator/azure-aks/azure-file-sc.yaml")
        val diskStorageClassName = diskStorageClassName(name)
        createStorageClassFromFile(resourceGroupName, diskStorageClassName, "operator/azure-aks/azure-disk-sc.yaml")

        getKubectlHelper().setDefaultStorageClass(fileStorageClassName)
    }

    private fun diskStorageClassName(name: String): String {
        return "${name}-disk-storage-class"
    }

    private fun fileStorageClassName(name: String): String {
        return "${name}-file-storage-class"
    }

    private fun createStorageClassFromFile(resourceGroupName: String, storageClassName: String, filePath: String) {
        if (!getKubectlHelper().hasStorageClass(storageClassName)) {
            project.logger.lifecycle("Create storage class: {}", storageClassName)
            val azureFileScTemplateFile = getTemplate(filePath)
            val azureFileScTemplate = azureFileScTemplateFile.readText(Charsets.UTF_8)
                .replace("{{NAME}}", storageClassName)
                .replace("{{RESOURCE_GROUP}}", resourceGroupName)
            azureFileScTemplateFile.writeText(azureFileScTemplate)
            getKubectlHelper().applyFile(azureFileScTemplateFile)
        } else {
            project.logger.lifecycle("Skipping creation of the existing storage class: {}", storageClassName)
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
            val result = ProcessUtil.executeCommand(
                project,
                "az aks list --output tsv | grep $clusterName", throwErrorOnFailure = false, logOutput = false
            )
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
            ProcessUtil.executeCommand(
                project,
                "az aks create --resource-group $groupName --name $clusterName --node-count ${
                    clusterNodeCount.getOrElse(2)
                } " +
                        "--generate-ssh-keys --enable-addons monitoring $additions"
            )
        }
    }

    private fun connectToCluster(name: String) {
        ProcessUtil.executeCommand(
            project,
            "az aks get-credentials --resource-group ${resourceGroupName(name)} --name ${aksClusterName(name)} --overwrite-existing"
        )
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
            ProcessUtil.executeCommand(
                project,
                "az group create --name $groupName --location $location"
            )
        }
    }

    fun resourceGroupName(name: String): String {
        return "${name}-group"
    }

    fun existsResourceGroup(groupName: String, location: String): Boolean {
        val result = ProcessUtil.executeCommand(
            project,
            "az group list --query \"[?location=='$location']\" --output tsv | grep $groupName",
            throwErrorOnFailure = false,
            logOutput = false
        )
        return result.contains(groupName)
    }

    private fun aksClusterName(name: String): String {
        return name
    }

    private fun deleteResourceGroup(name: String, groupName: String, location: String) {
        val clusterName = aksClusterName(name)
        project.logger.lifecycle("Delete resource group {} and AKS cluster {} ", groupName, clusterName)
        if (existsResourceGroup(groupName, location)) {
            project.logger.lifecycle("Delete resource group: {}", groupName)
            ProcessUtil.executeCommand(
                project,
                "az group delete --name $groupName --yes"
            )
        } else {
            project.logger.lifecycle("Skipping delete of the resource group: {}", groupName)
        }
    }

    private fun logoutAzCli(username: String?, password: String?) {
        if (username != null && password != null) {
            project.logger.lifecycle("Logout user")
            ProcessUtil.executeCommand(
                project,
                "az logout", throwErrorOnFailure = false
            )
        }
    }

    fun destroyClusterOnShutdown(existsResourceGroup: Boolean, name: String, groupName: String, location: String) {
        val azureAksProvider: AzureAksProvider = getProvider()

        if (azureAksProvider.destroyClusterOnShutdown.get()) {
            if (existsResourceGroup) {
                deleteResourceGroup(name, groupName, location)
            }

            getKubectlHelper().deleteCurrentContext()
            logoutAzCli(azureAksProvider.getAzUsername(), azureAksProvider.getAzPassword())
        }
    }

    override fun getFqdn(): String {
        val azureAksProvider: AzureAksProvider = getProvider()
        val location = azureAksProvider.location.get()
        return "${getHost()}.${location}.cloudapp.azure.com"
    }

    override fun getStorageClass(): String {
        return fileStorageClassName(getProvider().storageClass.getOrElse(getProvider().name.get()))
    }

    override fun getDbStorageClass(): String {
        return diskStorageClassName(getProvider().storageClass.getOrElse(getProvider().name.get()))
    }

}