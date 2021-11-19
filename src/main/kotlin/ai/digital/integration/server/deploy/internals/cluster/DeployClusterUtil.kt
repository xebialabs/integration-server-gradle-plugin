package ai.digital.integration.server.deploy.internals.cluster

import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import org.gradle.api.Project

class DeployClusterUtil {

    companion object {
        fun getProfile(project: Project): String {
            return DeployExtensionUtil.getExtension(project).cluster.get().profile
        }

        fun getTerraformProvider(project: Project): String {
            return DeployExtensionUtil.getExtension(project).clusterProfiles.terraform().activeProviderName.get()
        }

        fun getOperatorProvider(project: Project): String {
            return DeployExtensionUtil.getExtension(project).clusterProfiles.operator().activeProviderName.get()
        }
    }
}
