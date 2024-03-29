package ai.digital.integration.server.common.cluster.setup

import ai.digital.integration.server.common.cluster.Helper
import ai.digital.integration.server.common.constant.ClusterProfileName
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.profiles.HelmProfile
import ai.digital.integration.server.common.domain.profiles.OperatorProfile
import ai.digital.integration.server.common.domain.profiles.Profile
import ai.digital.integration.server.common.domain.providers.AwsOpenshiftProvider
import org.gradle.api.Project

open class AwsOpenshiftHelper(project: Project, productName: ProductName, val profile: Profile) : Helper(project, productName) {

    override fun getProvider(): AwsOpenshiftProvider {
        return when (val profileName = getProfileName()) {
            ClusterProfileName.OPERATOR.profileName -> {
                val operatorProfile = profile as OperatorProfile
                operatorProfile.awsOpenshift
            }
            ClusterProfileName.HELM.profileName -> {
                val helmProfile = profile as HelmProfile
                helmProfile.awsOpenshift
            }
            else -> {
                throw IllegalArgumentException("Provided profile name `$profileName` is not supported")
            }
        }
    }

    fun launchCluster() {
        createOcContext()
    }

    fun getApiServerUrl() = getProvider().apiServerURL.get()

    fun getOcLogin() = project.property("ocLogin")

    fun getOcPassword() = project.property("ocPassword")

    fun ocLogin() {
        exec("oc login ${getApiServerUrl()} --username ${getOcLogin()} --password \"${getOcPassword()}\"")
    }

    fun ocLogout() {
        try {
            exec("oc logout")
        } catch (e: Exception) {
            // ignore, if throws exception, it only means that already loged out, safe to ignore.
        }
    }

    private fun createOcContext() {
        project.logger.lifecycle("Updating kube config for Open Shift")
        exec("export KUBECONFIG=~/.kube/config")
        ocLogin()
    }

    override fun getStorageClass(): String {
        return getProvider().storageClass.getOrElse("aws-efs")
    }
}
