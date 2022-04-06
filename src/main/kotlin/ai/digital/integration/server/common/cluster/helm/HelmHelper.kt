package ai.digital.integration.server.common.cluster.helm

import ai.digital.integration.server.common.cluster.Helper
import ai.digital.integration.server.common.cluster.util.OperatorUtil
import ai.digital.integration.server.common.constant.OperatorHelmProviderName
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.constant.ServerConstants
import ai.digital.integration.server.common.domain.InfrastructureInfo
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.common.domain.profiles.HelmProfile
import ai.digital.integration.server.common.domain.profiles.IngressType
import ai.digital.integration.server.common.domain.profiles.OperatorProfile
import ai.digital.integration.server.common.domain.providers.Provider
import ai.digital.integration.server.common.util.*
import ai.digital.integration.server.deploy.domain.Worker
import ai.digital.integration.server.deploy.internals.CliUtil
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.internals.WorkerUtil
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import ai.digital.integration.server.release.internals.ReleaseExtensionUtil
import ai.digital.integration.server.release.tasks.cluster.ReleaseClusterUtil
import ai.digital.integration.server.release.util.ReleaseServerUtil
import kotlinx.coroutines.*
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@Suppress("UnstableApiUsage")
abstract class HelmHelper(project: Project, productName: ProductName) : Helper(project, productName){

    private val HELM_FOLDER_NAME: String = "xl-${getName()}-kubernetes-helm-chart"

    companion object {
        fun getHelmHelper(project: Project): HelmHelper {
            val productName = if (ReleaseServerUtil.isReleaseServerDefined(project)) {
                ProductName.RELEASE
            } else {
                ProductName.DEPLOY
            }
            return getHelmHelper(project, productName)
        }

        fun getHelmHelper(project: Project, productName: ProductName): HelmHelper {
            return when (val providerName = getHelmProvider(project, productName)) {
                OperatorHelmProviderName.AWS_EKS.providerName -> AwsEksHelmHelper(project, productName)
                OperatorHelmProviderName.AWS_OPENSHIFT.providerName -> AwsOpenshiftHelmHelper(project, productName)
                OperatorHelmProviderName.AZURE_AKS.providerName -> AzureAksHelmHelper(project, productName)
                OperatorHelmProviderName.GCP_GKE.providerName -> GcpGkeHelmHelper(project, productName)
                OperatorHelmProviderName.ON_PREMISE.providerName -> OnPremHelmHelper(project, productName)
                OperatorHelmProviderName.VMWARE_OPENSHIFT.providerName -> VmwareOpenshiftHelmHelper(project, productName)
                else -> {
                    throw IllegalArgumentException("Provided operator provider name `$providerName` is not supported. Choose one of ${
                        OperatorHelmProviderName.values().joinToString()
                    }")
                }
            }
        }

        private fun getHelmProvider(project: Project, productName: ProductName): String {
            return when (productName) {
                ProductName.DEPLOY -> DeployClusterUtil.getHelmProvider(project)
                ProductName.RELEASE -> ReleaseClusterUtil.getHelmProvider(project)
            }
        }
    }

    fun getHelmHomeDir(): String =
        project.buildDir.toPath().resolve(HELM_FOLDER_NAME).toAbsolutePath().toString()



    fun getProfile(): HelmProfile {
        return when (productName) {
            ProductName.DEPLOY -> DeployExtensionUtil.getExtension(project).clusterProfiles.helm()
            ProductName.RELEASE -> ReleaseExtensionUtil.getExtension(project).clusterProfiles.helm()
        }
    }


    private fun getConfigDir(): File {
        return when (productName) {
            ProductName.DEPLOY -> DeployServerUtil.getConfDir(project)
            ProductName.RELEASE -> ReleaseServerUtil.getConfDir(project)
        }
    }



}
