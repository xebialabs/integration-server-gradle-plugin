package ai.digital.integration.server.deploy.tasks.cluster.operator

import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.deploy.internals.cluster.operator.OperatorHelper
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

        val operatorHelper = OperatorHelper.getOperatorHelper(project)
        val branchClone = operatorHelper.getProvider().operatorBranch
                .map {
                    "-b $it"
                }.getOrElse("")
        ProcessUtil.executeCommand(
                "git clone git@github.com:xebialabs/xl-deploy-kubernetes-operator.git \"$dest\" $branchClone")
    }
}
