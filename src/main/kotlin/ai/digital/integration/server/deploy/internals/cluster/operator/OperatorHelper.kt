package ai.digital.integration.server.deploy.internals.cluster.operator

import ai.digital.integration.server.common.domain.profiles.OperatorProfile
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import org.gradle.api.Project

const val OPERATOR_FOLDER_NAME: String = "xl-deploy-kubernetes-operator"

const val CR_REL_PATH = "digitalai-deploy/kubernetes/daideploy_cr.yaml"

const val DEPLOYMENT_REL_PATH = "digitalai-deploy/kubernetes/template/deployment.yaml"

abstract class OperatorHelper(val project: Project) {
    fun getOperatorHomeDir(): String =
        project.buildDir.toPath().resolve(OPERATOR_FOLDER_NAME).toAbsolutePath().toString()

    fun getProfile(): OperatorProfile {
        return DeployExtensionUtil.getExtension(project).clusterProfiles.operator()
    }
}
