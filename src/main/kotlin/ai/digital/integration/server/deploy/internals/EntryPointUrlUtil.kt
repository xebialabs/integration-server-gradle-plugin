package ai.digital.integration.server.deploy.internals

import ai.digital.integration.server.common.cluster.DockerClusterHelperCreator
import ai.digital.integration.server.common.cluster.operator.OperatorHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import ai.digital.integration.server.release.tasks.cluster.ReleaseClusterUtil
import ai.digital.integration.server.release.util.ReleaseServerUtil
import org.gradle.api.Project

class EntryPointUrlUtil(
    val project: Project,
    val productName: ProductName
) {

    private fun getPropertyValue(
        key: String,
        clusterValue: String,
        auxiliaryServer: Boolean
    ): String {
        if (isClusterEnabled() && !auxiliaryServer) {
            return clusterValue
        }
        return when (productName) {
            ProductName.DEPLOY -> DeployServerUtil.readDeployitConfProperty(project, key)
            ProductName.RELEASE -> ReleaseServerUtil.readReleaseServerConfProperty(project, key)
        }
    }

    fun getHttpPort(auxiliaryServer: Boolean = false): String {
        if (isOperatorProvider() && !auxiliaryServer) {
            val operatorHelper = OperatorHelper.getOperatorHelper(project, productName)
            return operatorHelper.getPort()
        } else if (auxiliaryServer) {
            val operatorHelper = OperatorHelper.getOperatorHelper(project, ProductName.DEPLOY)
            val server = operatorHelper.getOperatorDeployServer(project)
            return server.httpPort.toString()
        }

        val dockerHelper = DockerClusterHelperCreator.create(project, productName)
        return getPropertyValue("http.port", dockerHelper.getClusterPublicPort(), auxiliaryServer)
    }

    fun getContextRoot(auxiliaryServer: Boolean = false): String {
        if (isOperatorProvider() && !auxiliaryServer) {
            val operatorHelper = OperatorHelper.getOperatorHelper(project, productName)
            return operatorHelper.getContextRoot()
        }

        return getPropertyValue("http.context.root", "", auxiliaryServer)
    }

    fun getHttpHost(auxiliaryServer: Boolean = false): String {
        if (isOperatorProvider() && !auxiliaryServer) {
            val operatorHelper = OperatorHelper.getOperatorHelper(project, productName)
            return operatorHelper.getFqdn()
        }

        return "localhost"
    }

    fun getUrl(auxiliaryServer: Boolean = false): String {
        val protocol = if (isTls()) "https" else "http"

        if (isOperatorProvider() && !auxiliaryServer) {
            val operatorHelper = OperatorHelper.getOperatorHelper(project, productName)
            return "$protocol://${operatorHelper.getFqdn()}"
        }

        val contextRoot = getContextRoot(auxiliaryServer)
        val host = getHttpHost(auxiliaryServer)
        val port = getHttpPort(auxiliaryServer)

        return "$protocol://$host:$port$contextRoot"
    }

    fun composeUrl(path: String, auxiliaryServer: Boolean = false): String {
        var url = getUrl(auxiliaryServer)
        var separator = "/"
        if (path.startsWith("/") || url.endsWith("/")) {
            separator = ""
            if (path.startsWith("/") && url.endsWith("/")) url = url.removeSuffix("/")

        }
        return "$url$separator$path"
    }

    private fun isClusterEnabled(): Boolean {
        return when (productName) {
            ProductName.DEPLOY -> DeployServerUtil.isClusterEnabled(project)
            ProductName.RELEASE -> ReleaseServerUtil.isClusterEnabled(project)
        }
    }

    private fun isOperatorProvider(): Boolean {
        return when (productName) {
            ProductName.DEPLOY -> DeployClusterUtil.isOperatorProvider(project)
            ProductName.RELEASE -> ReleaseClusterUtil.isOperatorProvider(project)
        }
    }

    private fun isTls(): Boolean {
        return when (productName) {
            ProductName.DEPLOY -> DeployServerUtil.isTls(project)
            ProductName.RELEASE -> false
        }
    }
}
