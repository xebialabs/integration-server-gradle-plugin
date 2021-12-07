package ai.digital.integration.server.deploy.tasks.kube.scanning

import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.common.util.KubeScanningUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class CheckingOutKubeBenchTask : DefaultTask() {

    companion object {
        const val NAME = "checkingOutKubeBench"
    }

    @TaskAction
    fun launch() {
        project.logger.lifecycle("Checking out kube-bench repo")
        cloneRepository()
    }

    private fun cloneRepository() {
        var tag = ""
        if (KubeScanningUtil.getKubeScanner(project).kubeBenchTagVersion != "latest") {
            tag = "--branch " +
                    "${KubeScanningUtil.getKubeScanner(project).kubeBenchTagVersion} "
        }
        ProcessUtil.executeCommand(project,
                "git clone " +
                        "${tag}" +
                        "https://github.com/aquasecurity/kube-bench.git" +
                        " ${KubeScanningUtil.getKubeBenchDir(project)}",
                logOutput = KubeScanningUtil.getKubeScanner(project).logOutput)

    }
}
