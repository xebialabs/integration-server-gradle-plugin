package ai.digital.integration.server.deploy.internals

import ai.digital.integration.server.deploy.internals.cluster.DeployClusterUtil
import ai.digital.integration.server.deploy.internals.cluster.DeployDockerClusterHelper
import ai.digital.integration.server.common.cluster.operator.OperatorHelper
import org.gradle.api.Project

class EntryPointUrlUtil {

    companion object {

        private fun getPropertyValue(
            project: Project,
            key: String,
            clusterValue: String,
            auxiliaryServer: Boolean
        ): String {
            if (DeployServerUtil.isClusterEnabled(project) && !auxiliaryServer) {
                return clusterValue
            }
            return DeployServerUtil.readDeployitConfProperty(project, key)
        }

        fun getHttpPort(project: Project, auxiliaryServer: Boolean = false): String {
            if (DeployClusterUtil.isOperatorProvider(project) && !auxiliaryServer) {
                val operatorHelper = OperatorHelper.getOperatorHelper(project)
                return operatorHelper.getPort()
            }

            val dockerHelper = DeployDockerClusterHelper(project)
            return getPropertyValue(project, "http.port", dockerHelper.getClusterPublicPort(), auxiliaryServer)
        }

        fun getContextRoot(project: Project, auxiliaryServer: Boolean = false): String {
            if (DeployClusterUtil.isOperatorProvider(project) && !auxiliaryServer) {
                val operatorHelper = OperatorHelper.getOperatorHelper(project)
                return operatorHelper.getContextRoot()
            }

            return getPropertyValue(project, "http.context.root", "", auxiliaryServer)
        }

        fun getHttpHost(project: Project, auxiliaryServer: Boolean = false): String {
            if (DeployClusterUtil.isOperatorProvider(project) && !auxiliaryServer) {
                val operatorHelper = OperatorHelper.getOperatorHelper(project)
                return operatorHelper.getFqdn()
            }

            return "localhost"
        }

        fun getUrl(project: Project, auxiliaryServer: Boolean = false): String {
            val protocol = if (DeployServerUtil.isTls(project)) "https" else "http"

            if (DeployClusterUtil.isOperatorProvider(project) && !auxiliaryServer) {
                val operatorHelper = OperatorHelper.getOperatorHelper(project)
                return "$protocol://${operatorHelper.getFqdn()}"
            }

            val contextRoot = getContextRoot(project, auxiliaryServer)
            val host = getHttpHost(project, auxiliaryServer)
            val port = getHttpPort(project, auxiliaryServer)

            return "$protocol://$host:$port$contextRoot"
        }

        fun composeUrl(project: Project, path: String, auxiliaryServer: Boolean = false): String {
            var url = getUrl(project, auxiliaryServer)
            var separator = "/"
            if (path.startsWith("/") || url.endsWith("/")) {
                separator = ""
                if (path.startsWith("/") && url.endsWith("/"))
                    url = url.removeSuffix("/")

            }
            return "$url$separator$path"
        }
    }
}
