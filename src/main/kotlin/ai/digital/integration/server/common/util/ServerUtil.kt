package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.domain.Server
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.release.util.ReleaseServerUtil
import org.gradle.api.Project

class ServerUtil(
    val project: Project,
    val productName: ProductName
) {
    fun getServer(): Server {
        return when (productName) {
            ProductName.DEPLOY -> DeployServerUtil.getServer(project)
            ProductName.RELEASE -> ReleaseServerUtil.getServer(project)
        }
    }
}
