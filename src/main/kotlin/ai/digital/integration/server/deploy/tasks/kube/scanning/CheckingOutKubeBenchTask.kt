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
        val benchVersion = KubeScanningUtil.getKubeScanner(project).kubeBenchTagVersion
        cloneRepository(benchVersion)
    }

    private fun cloneRepository(benchVersion: String) {
        var tag = ""
        if (benchVersion != "latest") {
            tag = "--branch " +
                    "$benchVersion "
        }
        ProcessUtil.executeCommand(project,
                "git clone " +
                        tag +
                        "https://github.com/aquasecurity/kube-bench.git" +
                        " ${KubeScanningUtil.getKubeBenchDir(project)}",
                logOutput = KubeScanningUtil.getKubeScanner(project).logOutput)

    }
}
