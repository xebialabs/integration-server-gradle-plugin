package ai.digital.integration.server.deploy.internals.cluster

import ai.digital.integration.server.common.constant.ClusterProfileName
import ai.digital.integration.server.common.constant.OperatorHelmProviderName
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

        fun getHelmProvider(project: Project): String {
            return DeployExtensionUtil.getExtension(project).clusterProfiles.helm().activeProviderName.get()
        }

        fun getOperatorProviderName(project: Project): OperatorHelmProviderName {
            return OperatorHelmProviderName.valueOfProviderName(getOperatorProvider(project))
        }

        fun getHelmProviderName(project: Project): OperatorHelmProviderName {
            return OperatorHelmProviderName.valueOfProviderName(getHelmProvider(project))
        }

        fun isOperatorProvider(project: Project): Boolean {
            return DeployExtensionUtil.getExtension(project).cluster.get().profile == ClusterProfileName.OPERATOR.profileName
        }

        fun isHelmProvider(project: Project): Boolean {
            return DeployExtensionUtil.getExtension(project).cluster.get().profile == ClusterProfileName.HELM.profileName
        }
    }
}
