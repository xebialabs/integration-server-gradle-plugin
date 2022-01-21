package ai.digital.integration.server.deploy.tasks.cluster.operator

import ai.digital.integration.server.common.cluster.operator.OperatorHelper
import ai.digital.integration.server.common.constant.ProductName
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
        val buildDirPath = project.buildDir.toPath().toAbsolutePath().toString()
        val dest = "$buildDirPath/xl-deploy-kubernetes-operator"

        val operatorHelper = OperatorHelper.getOperatorHelper(project, ProductName.DEPLOY)
        // it needs to be aligned with operatorImage default value
        val branchClone = operatorHelper.getProvider().operatorBranch.orElse("10.2.0")
                .map {
                    "-b $it"
                }.getOrElse("")
        ProcessUtil.executeCommand(
                "git clone git@github.com:xebialabs/xl-deploy-kubernetes-operator.git \"$dest\" $branchClone")
    }
}
