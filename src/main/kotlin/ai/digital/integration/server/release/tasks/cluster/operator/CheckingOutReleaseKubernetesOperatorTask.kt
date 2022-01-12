package ai.digital.integration.server.release.tasks.cluster.operator

import ai.digital.integration.server.common.util.ProcessUtil
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
        val buildDirPath = project.buildDir.toPath().toAbsolutePath().toString()
        val dest = "$buildDirPath/xl-release-kubernetes-operator"
        ProcessUtil.executeCommand(
            "git clone git@github.com:xebialabs/xl-release-kubernetes-operator.git \"$dest\"")
    }
}
