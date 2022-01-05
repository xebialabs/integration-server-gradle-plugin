package ai.digital.integration.server.deploy.internals.cluster.operator

import ai.digital.integration.server.common.domain.InfrastructureInfo
import ai.digital.integration.server.common.domain.providers.operator.GcpGkeProvider
import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.common.util.YamlFileUtil
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.gradle.api.Project
import org.gradle.api.provider.Property
import java.io.File

open class GcpGkeHelper(project: Project) : OperatorHelper(project) {

    fun launchCluster() {
        val gcpGkeProvider: GcpGkeProvider = getProvider()
        val projectName = gcpGkeProvider.projectName.get()
        val name = gcpGkeProvider.name.get()
        val skipExisting = gcpGkeProvider.skipExisting.get()
        val regionZone = gcpGkeProvider.regionZone.get()
        val accountName = gcpGkeProvider.accountName.get()

        validateGCloudCli()
        loginGCloudCli(accountName, gcpGkeProvider.getAccountCredFile())
        changeDefaultProject(projectName)
        changeDefaultRegionZone(regionZone)

        createCluster(accountName, projectName, name, regionZone, gcpGkeProvider.clusterNodeCount, gcpGkeProvider.clusterNodeVmSize, gcpGkeProvider.kubernetesVersion, skipExisting)
        connectToCluster(accountName, projectName, name, regionZone)
        val kubeContextInfo = getCurrentContextInfo(accountName, projectName)

        useCustomStorageClass(getStorageClass())

        updateOperatorDeployment()
        updateOperatorDeploymentCr()
        updateInfrastructure(kubeContextInfo)
        updateOperatorCrValues()
        updateCrValues()

        applyYamlFiles()
        waitForDeployment()
        waitForMasterPods()
        waitForWorkerPods()
        val ip = getKubectlHelper().getServiceExternalIp("service/dai-xld-nginx-ingress-controller")
        applyDnsOpenApi(ip)

        createClusterMetadata()
        waitForBoot()
    }

    fun shutdownCluster() {
        val gcpGkeProvider: GcpGkeProvider = getProvider()
        val projectName = gcpGkeProvider.projectName.get()
        val name = gcpGkeProvider.name.get()
        val regionZone = gcpGkeProvider.regionZone.get()
        val accountName = gcpGkeProvider.accountName.get()

        if (existsCluster(accountName, projectName, name, regionZone)) {
            undeployCluster()
            deleteCluster(accountName, projectName, name, regionZone)
        }
        deleteDnsOpenApi()

        getKubectlHelper().deleteCurrentContext()
        logoutGCloudCli(gcpGkeProvider.accountName.get())
    }

    override fun getProviderHomeDir(): String {
        return "${getOperatorHomeDir()}/deploy-operator-gcp-gke"
    }

    override fun getProvider(): GcpGkeProvider {
        return getProfile().gcpGke
    }

    override fun getFqdn(): String {
        return "${getHost()}.endpoints.${getProvider().projectName.get()}.cloud.goog"
    }

    private fun validateGCloudCli() {
        val result = ProcessUtil.executeCommand(project,
                "gcloud -v", throwErrorOnFailure = false, logOutput = false)
        if (!result.contains("Google Cloud SDK")) {
            throw RuntimeException("No Google Cloud SDK \"gcloud\" in the path. Please verify your installation")
        }
    }

    private fun loginGCloudCli(accountName: String, accountCredFile: String) {
        project.logger.lifecycle("Login account $accountName")
        val additions = if(accountCredFile.isNotBlank())
            " --cred-file=\"${File(accountCredFile).absolutePath}\""
        else
            ""
        ProcessUtil.executeCommand(project,
                "gcloud auth login $accountName $additions --quiet")
    }

    private fun logoutGCloudCli(accountName: String) {
        project.logger.lifecycle("Revoke account $accountName")
        ProcessUtil.executeCommand(project,
                "gcloud auth revoke $accountName --quiet", throwErrorOnFailure = false)
    }

    private fun changeDefaultProject(projectName: String) {
        project.logger.lifecycle("Change default project to $projectName")
        ProcessUtil.executeCommand(project,
                "gcloud config set project $projectName")
    }

    private fun changeDefaultRegionZone(regionZone: String) {
        project.logger.lifecycle("Change default region to $regionZone")
        ProcessUtil.executeCommand(project,
                "gcloud config set compute/zone $regionZone")
    }

    private fun createCluster(accountName: String, projectName: String, name: String, regionZone: String, clusterNodeCount: Property<Int>, clusterNodeVmSize: Property<String>, kubernetesVersion: Property<String>, skipExisting: Boolean) {
        val shouldSkipExisting = if (skipExisting) {
            existsCluster(accountName, projectName, name, regionZone)
        } else {
            false
        }
        if (shouldSkipExisting) {
            project.logger.lifecycle("Skipping creation of the existing cluster: {}", name)
        } else {
            project.logger.lifecycle("Create cluster: {}", name)

            val additions = clusterNodeVmSize.map { " --machine-type \"$it\"" }.getOrElse("") +
                    kubernetesVersion.map { " --cluster-version \"$it\"" }.getOrElse(" --cluster-version \"1.20.11-gke.1801\"")

            ProcessUtil.executeCommand(project,
                    "gcloud beta container --account \"$accountName\" --project \"$projectName\" clusters create \"$name\" --zone  \"$regionZone\" " +
                            "--release-channel \"regular\" " +
                            "--num-nodes \"${clusterNodeCount.getOrElse(3)}\" --image-type \"COS_CONTAINERD\" --metadata disable-legacy-endpoints=true " +
                            "--logging=SYSTEM,WORKLOAD --monitoring=SYSTEM --enable-ip-alias --no-enable-master-authorized-networks " +
                            "--addons HorizontalPodAutoscaling,HttpLoadBalancing,GcePersistentDiskCsiDriver --enable-autoupgrade --enable-autorepair " +
                            "--enable-shielded-nodes $additions")
        }
    }

    private fun existsCluster(accountName: String, projectName: String, name: String, regionZone: String): Boolean {
        val result = ProcessUtil.executeCommand(project,
                "gcloud beta container --account \"$accountName\" --project \"$projectName\" clusters list --zone \"$regionZone\"", throwErrorOnFailure = false, logOutput = false)
        return result.contains(name)
    }

    private fun deleteCluster(accountName: String, projectName: String, name: String, regionZone: String) {
        if (existsCluster(accountName, projectName, name, regionZone)) {
            project.logger.lifecycle("Delete cluster (async): {}", name)
            ProcessUtil.executeCommand(project,
                    "gcloud beta container --account \"$accountName\" --project \"$projectName\" clusters delete \"$name\" --zone \"$regionZone\" --quiet", throwErrorOnFailure = false)
        } else {
            project.logger.lifecycle("Skipping delete of the cluster: {}", name)
        }
    }

    private fun connectToCluster(accountName: String, projectName: String, name: String, regionZone: String) {
        ProcessUtil.executeCommand(project,
                "gcloud beta container --account \"$accountName\" --project \"$projectName\" clusters get-credentials \"$name\" --zone \"$regionZone\"")
    }

    private fun getCurrentContextInfo(accountName: String, projectName: String): Pair<InfrastructureInfo, String> {
        val kubectlHelper = getKubectlHelper()
        val context = kubectlHelper.getCurrentContext()
        val cluster = kubectlHelper.getContextCluster(context)
        val user = kubectlHelper.getContextUser(context)

        val info = InfrastructureInfo(
                clusterName = cluster,
                userName = user,
                apiServerURL = kubectlHelper.getClusterServer(cluster),
                caCert = kubectlHelper.getClusterCertificateAuthorityData(cluster),
                tlsCert = null,
                tlsPrivateKey = null
        )
        val gcloudConfig = ProcessUtil.executeCommand(project,
                "gcloud config --account \"$accountName\" --project \"$projectName\" config-helper --format=yaml")

        val gcloudConfigMap = ObjectMapper(YAMLFactory.builder().build()).readValue(gcloudConfig, MutableMap::class.java)
        val accessToken = (gcloudConfigMap["credential"] as Map<*, *>)["access_token"] as String

        return Pair(info, accessToken)
    }

    private fun updateInfrastructure(kubeContextInfo: Pair<InfrastructureInfo, String>) {
        val file = File(getProviderHomeDir(), OPERATOR_INFRASTRUCTURE_PATH)
        val pairs = mutableMapOf<String, Any>(
                "spec[0].children[0].apiServerURL" to kubeContextInfo.first.apiServerURL!!,
                "spec[0].children[0].caCert" to kubeContextInfo.first.caCert!!,
                "spec[0].children[0].token" to kubeContextInfo.second
        )
        YamlFileUtil.overlayFile(file, pairs)
    }

    private fun updateCrValues() {
        val file = File(getProviderHomeDir(), OPERATOR_CR_VALUES_REL_PATH)
        val pairs: MutableMap<String, Any> = mutableMapOf(
                "spec.ingress.hosts" to listOf(getFqdn())
        )
        YamlFileUtil.overlayFile(file, pairs, minimizeQuotes = false)
    }

    private fun useCustomStorageClass(storageClassName: String) {
        if (!getKubectlHelper().hasStorageClass(storageClassName) && "standard" != storageClassName) {
            project.logger.lifecycle("Use storage class: {}", storageClassName)
            getKubectlHelper().setDefaultStorageClass(storageClassName)
        } else {
            project.logger.lifecycle("Skipping using of storage class: {}", storageClassName)
        }
    }

    private fun applyDnsOpenApi(ip: String) {
        val gcpGkeProvider: GcpGkeProvider = getProvider()
        val projectName = gcpGkeProvider.projectName.get()
        val name = gcpGkeProvider.name.get()
        val accountName = gcpGkeProvider.accountName.get()
        val serviceName = getFqdn()

        val dnsOpenApiTemplateFile = getTemplate("operator/gcp-gke/dns-openapi.yaml")
        val dnsOpenApiTemplate = dnsOpenApiTemplateFile.readText(Charsets.UTF_8)
                .replace("{{NAME}}", name)
                .replace("{{PROJECT_ID}}", projectName)
                .replace("{{IP}}", ip)
        dnsOpenApiTemplateFile.writeText(dnsOpenApiTemplate)
        ProcessUtil.executeCommand(project,
                "gcloud endpoints --account \"$accountName\" --project \"$projectName\" services undelete \"$serviceName\"", throwErrorOnFailure = false, logOutput = false)
        ProcessUtil.executeCommand(project,
                "gcloud endpoints --account \"$accountName\" --project \"$projectName\" services deploy \"${dnsOpenApiTemplateFile.absolutePath}\" --force")
    }

    private fun existsDnsOpenApi(accountName: String, projectName: String): Boolean {
        val serviceName = getFqdn()
        val result = ProcessUtil.executeCommand(project,
                "gcloud endpoints --account \"$accountName\" --project \"$projectName\" services list", throwErrorOnFailure = false, logOutput = false)
        return result.contains(serviceName)
    }

    private fun deleteDnsOpenApi() {
        val gcpGkeProvider: GcpGkeProvider = getProvider()
        val projectName = gcpGkeProvider.projectName.get()
        val accountName = gcpGkeProvider.accountName.get()
        if (existsDnsOpenApi(accountName, projectName)) {
            ProcessUtil.executeCommand(project,
                    "gcloud endpoints --account \"$accountName\" --project \"$projectName\" services delete \"${getFqdn()}\" --quiet", throwErrorOnFailure = false)
        }
    }
}
