package ai.digital.integration.server.release.internals.cluster

import ai.digital.integration.server.common.cluster.DockerClusterHelper
import ai.digital.integration.server.release.util.ReleaseServerUtil
import org.gradle.api.Project

class ReleaseDockerClusterHelper(val project: Project) : DockerClusterHelper {

    override fun getClusterPublicPort(): String {
        return ReleaseServerUtil.getCluster(project).publicPort.toString()
    }

}
