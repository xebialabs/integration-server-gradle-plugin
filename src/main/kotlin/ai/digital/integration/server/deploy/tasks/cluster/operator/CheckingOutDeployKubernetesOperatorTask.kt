package ai.digital.integration.server.deploy.tasks.cluster.operator

import ai.digital.integration.server.common.cluster.operator.OperatorHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.util.GitUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class CheckingOutDeployKubernetesOperatorTask : DefaultTask() {

    companion object {
        const val NAME = "checkingOutDeployKubernetesOperator"
    }

    @TaskAction
    fun launch() {
        val operatorHelper = OperatorHelper.getOperatorHelper(project, ProductName.DEPLOY)
        // it needs to be aligned with operatorImage default value
        val branch = operatorHelper.getProvider().operatorBranch.getOrElse("master")
        project.logger.lifecycle("Checking out xl-deploy-kubernetes-operator branch $branch")
        cloneRepository(branch)
    }

    private fun cloneRepository(branch: String) {
        GitUtil.checkout("xl-deploy-kubernetes-operator", project.buildDir.toPath(), branch)
    }
}
