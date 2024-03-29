package ai.digital.integration.server.common.cluster

import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.internals.cluster.DeployDockerClusterHelper
import ai.digital.integration.server.release.internals.cluster.ReleaseDockerClusterHelper
import org.gradle.api.Project

class DockerClusterHelperCreator {
    companion object {
        fun create(project: Project, productName: ProductName): DockerClusterHelper {
            return when (productName) {
                ProductName.DEPLOY -> DeployDockerClusterHelper(project)
                ProductName.RELEASE -> ReleaseDockerClusterHelper(project)
            }
        }
    }
}
