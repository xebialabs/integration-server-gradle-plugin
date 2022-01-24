package ai.digital.integration.server.release.tasks.cluster.operator

import ai.digital.integration.server.common.cluster.operator.OperatorHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.util.GitUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class CheckingOutReleaseKubernetesOperatorTask : DefaultTask() {

    companion object {
        const val NAME = "checkingOutReleaseKubernetesOperator"
    }

    @TaskAction
    fun launch() {
        project.logger.lifecycle("Checking out xl-release-kubernetes-operator")
        cloneRepository()
    }

    private fun cloneRepository() {
        val operatorHelper = OperatorHelper.getOperatorHelper(project, ProductName.RELEASE)
        GitUtil.checkout("xl-release-kubernetes-operator", project.buildDir.toPath(),
                // it needs to be aligned with operatorImage default value
                operatorHelper.getProvider().operatorBranch.orElse("10.2.0").orNull)
    }
}
