package ai.digital.integration.server.release.tasks.cluster

import ai.digital.integration.server.common.constant.ClusterProfileName
import ai.digital.integration.server.common.constant.OperatorHelmProviderName
import ai.digital.integration.server.release.internals.ReleaseExtensionUtil
import org.gradle.api.Project

class ReleaseClusterUtil {

    companion object {
        fun getProfile(project: Project): String {
            return ReleaseExtensionUtil.getExtension(project).cluster.get().profile
        }

        fun getTerraformProvider(project: Project): String {
            return ReleaseExtensionUtil.getExtension(project).clusterProfiles.terraform().activeProviderName.get()
        }

        fun getOperatorProvider(project: Project): String {
            return ReleaseExtensionUtil.getExtension(project).clusterProfiles.operator().activeProviderName.get()
        }

        fun getOperatorProviderName(project: Project): OperatorHelmProviderName {
            return OperatorHelmProviderName.valueOfProviderName(getOperatorProvider(project))
        }

        fun isOperatorProvider(project: Project): Boolean {
            return ReleaseExtensionUtil.getExtension(project).cluster.get().profile == ClusterProfileName.OPERATOR.profileName
        }
    }
}
