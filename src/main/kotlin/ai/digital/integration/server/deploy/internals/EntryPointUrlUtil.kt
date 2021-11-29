package ai.digital.integration.server.deploy.internals

import ai.digital.integration.server.deploy.internals.cluster.DeployDockerClusterHelper
import org.gradle.api.Project

class EntryPointUrlUtil {

    companion object {

        private fun getPropertyValue(
            project: Project,
            key: String,
            clusterValue: String,
            auxiliaryServer: Boolean = false
        ): String {
            val helper = DeployDockerClusterHelper(project)
            if (helper.isClusterEnabled() && !auxiliaryServer) {
                return clusterValue
            }
            return DeployServerUtil.readDeployitConfProperty(project, key)
        }

        fun getHttpPort(project: Project, auxiliaryServer: Boolean = false): String {
            val helper = DeployDockerClusterHelper(project)
            return getPropertyValue(project, "http.port", helper.getClusterPublicPort(), auxiliaryServer)
        }

        fun getContextRoot(project: Project): String {
            return getPropertyValue(project, "http.context.root", "")
        }

        fun getHttpHost(): String {
            return "localhost"
        }

        fun getUrl(project: Project, auxiliaryServer: Boolean = false): String {
            val contextRoot = getContextRoot(project)
            val host = getHttpHost()
            val port = getHttpPort(project, auxiliaryServer)
            val protocol = if (DeployServerUtil.isTls(project)) "https" else "http"

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
