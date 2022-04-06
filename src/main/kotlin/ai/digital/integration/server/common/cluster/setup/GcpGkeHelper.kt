package ai.digital.integration.server.common.cluster.setup

import ai.digital.integration.server.common.cluster.Helper
import ai.digital.integration.server.common.cluster.operator.OperatorHelper
import ai.digital.integration.server.common.constant.ClusterProfileName
import ai.digital.integration.server.common.constant.ProductName

import ai.digital.integration.server.common.domain.providers.GcpGkeProvider
import ai.digital.integration.server.common.util.ProcessUtil
import org.gradle.api.Project
import org.gradle.api.provider.Property
import java.io.File

open class GcpGkeHelper(project: Project, productName: ProductName) : Helper(project, productName) {

    override fun getProvider(): GcpGkeProvider {
        val profileName = getProfileName()
        if (profileName == ClusterProfileName.OPERATOR.profileName) {
            return OperatorHelper.getOperatorHelper(project, productName).getProfile().gcpGke
        } else {
            throw IllegalArgumentException("Provided profile name `$profileName` is not supported")
        }
    }

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

        createCluster(
            accountName,
            projectName,
            name,
            regionZone,
            gcpGkeProvider.clusterNodeCount,
            gcpGkeProvider.clusterNodeVmSize,
            gcpGkeProvider.kubernetesVersion,
            skipExisting
        )
        connectToCluster(accountName, projectName, name, regionZone)
        useCustomStorageClass(getStorageClass())
    }

    private fun validateGCloudCli() {
        val result = ProcessUtil.executeCommand(
            project,
            "gcloud -v", throwErrorOnFailure = false, logOutput = false
        )
        if (!result.contains("Google Cloud SDK")) {
            throw RuntimeException("No Google Cloud SDK \"gcloud\" in the path. Please verify your installation")
        }
    }

    private fun loginGCloudCli(accountName: String, accountCredFile: String) {
        project.logger.lifecycle("Login account $accountName")
        val additions = if (accountCredFile.isNotBlank())
            " --cred-file=\"${File(accountCredFile).absolutePath}\""
        else
            ""
        ProcessUtil.executeCommand(
            project,
            "gcloud auth login $accountName $additions --quiet"
        )
    }

    private fun changeDefaultProject(projectName: String) {
        project.logger.lifecycle("Change default project to $projectName")
        ProcessUtil.executeCommand(
            project,
            "gcloud config set project $projectName"
        )
    }

    private fun changeDefaultRegionZone(regionZone: String) {
        project.logger.lifecycle("Change default region to $regionZone")
        ProcessUtil.executeCommand(
            project,
            "gcloud config set compute/zone $regionZone"
        )
    }

    private fun createCluster(
        accountName: String,
        projectName: String,
        name: String,
        regionZone: String,
        clusterNodeCount: Property<Int>,
        clusterNodeVmSize: Property<String>,
        kubernetesVersion: Property<String>,
        skipExisting: Boolean
    ) {
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
                    kubernetesVersion.map { " --cluster-version \"$it\"" }
                        .getOrElse(" --cluster-version \"1.21.5-gke.1802\"")

            ProcessUtil.executeCommand(
                project,
                "gcloud beta container --account \"$accountName\" --project \"$projectName\" clusters create \"$name\" --zone  \"$regionZone\" " +
                        "--release-channel \"regular\" " +
                        "--num-nodes \"${clusterNodeCount.getOrElse(3)}\" --image-type \"COS_CONTAINERD\" --metadata disable-legacy-endpoints=true " +
                        "--logging=SYSTEM,WORKLOAD --monitoring=SYSTEM --enable-ip-alias --no-enable-master-authorized-networks " +
                        "--addons HorizontalPodAutoscaling,HttpLoadBalancing,GcpFilestoreCsiDriver --enable-autoupgrade --enable-autorepair " +
                        "--enable-shielded-nodes $additions"
            )
        }
    }

    fun existsCluster(accountName: String, projectName: String, name: String, regionZone: String): Boolean {
        val result = ProcessUtil.executeCommand(
            project,
            "gcloud beta container --account \"$accountName\" --project \"$projectName\" clusters list --zone \"$regionZone\"",
            throwErrorOnFailure = false,
            logOutput = false
        )
        return result.contains(name)
    }

    private fun connectToCluster(accountName: String, projectName: String, name: String, regionZone: String) {
        ProcessUtil.executeCommand(
            project,
            "gcloud beta container --account \"$accountName\" --project \"$projectName\" clusters get-credentials \"$name\" --zone \"$regionZone\""
        )
    }

    private fun useCustomStorageClass(storageClassName: String) {
        if (!getKubectlHelper().hasStorageClass(storageClassName) && "standard" != storageClassName) {
            project.logger.lifecycle("Use storage class: {}", storageClassName)
            getKubectlHelper().setDefaultStorageClass(storageClassName)
        } else {
            project.logger.lifecycle("Skipping using of storage class: {}", storageClassName)
        }
    }

    fun applyDnsOpenApi(ip: String, fqdn: String = getFqdn(), host: String = getHost(), nameSpace: String = "default") {
        val gcpGkeProvider: GcpGkeProvider = getProvider()
        val projectName = gcpGkeProvider.projectName.get()
        val name = gcpGkeProvider.name.get()
        val accountName = gcpGkeProvider.accountName.get()
        val dnsOpenApiTemplateFile = getTemplate("operator/gcp-gke/dns-openapi.yaml")
        val dnsOpenApiTemplate = dnsOpenApiTemplateFile.readText(Charsets.UTF_8)
            .replace("{{NAME}}", name)
            .replace("{{PROJECT_ID}}", projectName)
            .replace("{{PRODUCT_NAME}}", productName.shortName)
            .replace("{{HOST}}", host)
            .replace("{{NAMESPACE}}", nameSpace)
            .replace("{{IP}}", ip)
        dnsOpenApiTemplateFile.writeText(dnsOpenApiTemplate)
        ProcessUtil.executeCommand(
            project,
                "gcloud endpoints --account \"$accountName\" --project \"$projectName\" services undelete \"$fqdn\"",
            throwErrorOnFailure = false,
            logOutput = false
        )
        ProcessUtil.executeCommand(
            project,
            "gcloud endpoints --account \"$accountName\" --project \"$projectName\" services deploy \"${dnsOpenApiTemplateFile.absolutePath}\" --force"
        )
    }

    private fun deleteCluster(accountName: String, projectName: String, name: String, regionZone: String) {
        if (existsCluster(accountName, projectName, name, regionZone)) {
            project.logger.lifecycle("Delete cluster (async): {}", name)
            ProcessUtil.executeCommand(
                project,
                "gcloud beta container --account \"$accountName\" --project \"$projectName\" clusters delete \"$name\" --zone \"$regionZone\" --quiet",
                throwErrorOnFailure = false
            )
        } else {
            project.logger.lifecycle("Skipping delete of the cluster: {}", name)
        }
    }

    private fun logoutGCloudCli(accountName: String) {
        project.logger.lifecycle("Revoke account $accountName")
        ProcessUtil.executeCommand(
            project,
            "gcloud auth revoke $accountName --quiet", throwErrorOnFailure = false
        )
    }

    fun destroyClusterOnShutdown(
        existsCluster: Boolean,
        accountName: String,
        projectName: String,
        name: String,
        regionZone: String,
        fqdn: String = getFqdn()
    ) {
        val gcpGkeProvider: GcpGkeProvider = getProvider()
        if (gcpGkeProvider.destroyClusterOnShutdown.get()) {
            if (existsCluster) {
                deleteCluster(accountName, projectName, name, regionZone)
            }
            deleteDnsOpenApi(fqdn)
            getKubectlHelper().deleteCurrentContext()
            logoutGCloudCli(gcpGkeProvider.accountName.get())
        }
    }

    private fun deleteDnsOpenApi(fqdn: String) {
        val gcpGkeProvider: GcpGkeProvider = getProvider()
        val projectName = gcpGkeProvider.projectName.get()
        val accountName = gcpGkeProvider.accountName.get()
        if (existsDnsOpenApi(accountName, projectName, fqdn)) {
            ProcessUtil.executeCommand(
                project,
                "gcloud endpoints --account \"$accountName\" --project \"$projectName\" services delete \"${fqdn}\" --quiet",
                throwErrorOnFailure = false
            )
        }
    }

    private fun existsDnsOpenApi(accountName: String, projectName: String, fqdn: String): Boolean {
        val result = ProcessUtil.executeCommand(
            project,
            "gcloud endpoints --account \"$accountName\" --project \"$projectName\" services list",
            throwErrorOnFailure = false,
            logOutput = false
        )
        return result.contains(fqdn)
    }

    override fun getFqdn(): String {
        return "${productName.shortName}-${getHost()}.endpoints.${getProvider().projectName.get()}.cloud.goog"
    }
}