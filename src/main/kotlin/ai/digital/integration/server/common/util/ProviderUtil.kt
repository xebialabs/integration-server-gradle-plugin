package ai.digital.integration.server.common.util

import ai.digital.integration.server.common.domain.Provider
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import org.gradle.api.Project

class ProviderUtil {
    companion object {
        fun hasProviders(project: Project): Boolean {
            return !DeployExtensionUtil.getExtension(project).providers.isEmpty()
        }

        fun getFirstProvider(project: Project): Provider {
            return DeployExtensionUtil.getExtension(project).providers.first()
        }

        fun getProviders(project: Project): List<Provider> {
            return DeployExtensionUtil.getExtension(project).providers.map { provider: Provider ->
                provider
            }
        }
    }
}