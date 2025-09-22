package ai.digital.integration.server.common.cluster

import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.deploy.internals.cluster.DeployDockerClusterHelper
import ai.digital.integration.server.release.internals.cluster.ReleaseDockerClusterHelper
import org.gradle.api.Project
import org.gradle.process.ExecOperations

class DockerClusterHelperCreator {
    companion object {

        private fun getExecOperations(project: Project): ExecOperations {
            return project.extensions.getByName("execOperations") as ExecOperations
        }
        fun create(project: Project, productName: ProductName): DockerClusterHelper {
            return when (productName) {
                ProductName.DEPLOY -> DeployDockerClusterHelper(getExecOperations(project), project)
                ProductName.RELEASE -> ReleaseDockerClusterHelper(project)
            }
        }
    }
}
