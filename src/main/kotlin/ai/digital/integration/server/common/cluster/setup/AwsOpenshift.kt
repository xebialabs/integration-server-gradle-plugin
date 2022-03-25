package ai.digital.integration.server.common.cluster.setup

import ai.digital.integration.server.common.cluster.Helper
import ai.digital.integration.server.common.cluster.operator.OperatorHelper
import ai.digital.integration.server.common.constant.ClusterProfileName
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.providers.AwsOpenshiftProvider
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import org.gradle.api.Project

open class AwsOpenshift(project: Project, productName: ProductName) : Helper(project, productName) {

    fun launchCluster() {
        createOcContext()
        ocLogin()
    }

    fun getApiServerUrl() = getProvider().apiServerURL.get()

    fun getOcLogin() = project.property("ocLogin")

    fun getOcPassword() = project.property("ocPassword")

    fun ocLogin() {
        exec("oc login ${getApiServerUrl()} --username ${getOcLogin()} --password \"${getOcPassword()}\"")
    }

    override fun getProvider(): AwsOpenshiftProvider {
        val profileName = DeployClusterUtil.getProfile(project)
        if (profileName == ClusterProfileName.OPERATOR.profileName) {
            return OperatorHelper.getOperatorHelper(project, productName).getProfile().awsOpenshift
        } else {
            throw IllegalArgumentException("Provided profile name `$profileName` is not supported")
        }
    }

    private fun createOcContext() {
        project.logger.lifecycle("Updating kube config for Open Shift")
        exec("export KUBECONFIG=~/.kube/config")
        ocLogin()
    }
}