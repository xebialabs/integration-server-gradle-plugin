package ai.digital.integration.server.deploy.tasks.cluster.operator

import ai.digital.integration.server.common.cluster.operator.OperatorHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.util.GitUtil
import ai.digital.integration.server.common.util.ProcessUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class CheckingOutDeployKubernetesOperatorTask : DefaultTask() {

    companion object {
        const val NAME = "checkingOutDeployKubernetesOperator"
    }

    @TaskAction
    fun launch() {
        project.logger.lifecycle("Checking out xl-deploy-kubernetes-operator")
        cloneRepository()
    }

    private fun cloneRepository() {
        val operatorHelper = OperatorHelper.getOperatorHelper(project, ProductName.DEPLOY)
        GitUtil.checkout("xl-deploy-kubernetes-operator", project.buildDir.toPath(),
                // it needs to be aligned with operatorImage default value
                operatorHelper.getProvider().operatorBranch.orElse("10.2.0").orNull)
    }
}
