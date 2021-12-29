package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.domain.Infrastructure
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import org.gradle.api.Project

class InfrastructureUtil {
    companion object {
        fun getInfrastructures(project: Project): List<Infrastructure> {
            return DeployExtensionUtil.getExtension(project).infrastructures.toList()
        }
    }
}