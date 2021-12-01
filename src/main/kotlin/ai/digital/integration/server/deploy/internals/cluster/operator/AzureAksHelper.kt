package ai.digital.integration.server.deploy.internals.cluster.operator

import ai.digital.integration.server.common.domain.InfrastructureInfo
import ai.digital.integration.server.common.domain.providers.operator.AzureAksProvider
import ai.digital.integration.server.common.util.*
import org.gradle.api.Project
import java.io.File
import java.nio.file.Paths

// TODO login with az
open class AzureAksHelper(project: Project) : OperatorHelper(project) {

    fun launchCluster() {
        val azureAksProvider: AzureAksProvider = getProvider()
        val name = azureAksProvider.name.get()
        val skipExisting = azureAksProvider.skipExisting.get()

        createResourceGroup(name, azureAksProvider.location.get(), skipExisting)
        createCluster(name, azureAksProvider.clusterNodeCount.get(), skipExisting)
        connectToCluster(name)
        val kubeContextInfo = KubeCtlUtil.getCurrentContextInfo(project)
        createStorageClass(name)

        updateControllerManager()
        updateOperatorDeployment()
        updateOperatorDeploymentCr()
        updateInfrastructure(kubeContextInfo)

        applyDigitalAi()
        waitForDeployment()
    }

    fun shutdownCluster() {
        val azureAksProvider: AzureAksProvider = getProvider()
        val name = azureAksProvider.name.get()

        val groupName = resourceGroupName(name)
        val clusterName = aksClusterName(name)

        project.logger.lifecycle("Undeploy operator")
        undeployCis()

        project.logger.lifecycle("Delete resource group {} and AKS cluster {} ", groupName, clusterName)
//        ProcessUtil.executeCommand(project,
//                "az group delete --name $groupName --yes")
//
//        KubeCtlUtil.deleteCurrentContext(project)
    }

    override fun updateInfrastructure(infraInfo: InfrastructureInfo) {
        val file = File(getProviderHomeDir(), OPERATOR_INFRASTRUCTURE_PATH)
        val pairs = mutableMapOf<String, Any>(
                "spec[0].children[0].apiServerURL" to kubeContextInfo.apiServerURL,
                "spec[0].children[0].caCert" to kubeContextInfo.caCert,
                "spec[0].children[0].tlsCert" to kubeContextInfo.tlsCert,
                "spec[0].children[0].tlsPrivateKey" to kubeContextInfo.tlsPrivateKey
        )
        YamlFileUtil.overlayFile(file, pairs)
    }

    override fun getProviderHomeDir(): String {
        return "${getOperatorHomeDir()}/deploy-operator-azure-aks"
    }

    override fun getProvider(): AzureAksProvider {
        return getProfile().azureAks
    }

    fun createStorageClass(name: String) {
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

    fun createResourceGroup(name: String, location: String, skipExisting: Boolean) {
        val groupName = resourceGroupName(name)
        var shouldSkipExisting = false
        if (skipExisting) {
            val result = ProcessUtil.executeCommand(project,
                    "az group list --query \"[?location=='$location']\" --output tsv | grep $groupName")
            if (result.contains(groupName)) {
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

    fun createCluster(name: String, clusterNodeCount: Int, skipExisting: Boolean) {
        val groupName = resourceGroupName(name)
        val clusterName = aksClusterName(name)
        var shouldSkipExisting = false
        if (skipExisting) {
            val result = ProcessUtil.executeCommand(project,
                    "az aks list --output tsv | grep $clusterName")
            if (result.contains(clusterName)) {
                shouldSkipExisting = true
            }
        }
        if (shouldSkipExisting) {
            project.logger.lifecycle("Skipping creation of the existing AKS cluster: {}", clusterName)
        } else {
            project.logger.lifecycle("Create AKS cluster: {}", clusterName)
            ProcessUtil.executeCommand(project,
                    "az aks create --resource-group $groupName --name $clusterName --node-count $clusterNodeCount --generate-ssh-keys --enable-addons monitoring")
        }
    }

    fun connectToCluster(name: String) {
        ProcessUtil.executeCommand(project,
                "az aks get-credentials --resource-group ${resourceGroupName(name)} --name ${aksClusterName(name)} --overwrite-existing")
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

    private fun aksSshKeyName(name: String): String {
        return "${name}-ssh-key"
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
}

