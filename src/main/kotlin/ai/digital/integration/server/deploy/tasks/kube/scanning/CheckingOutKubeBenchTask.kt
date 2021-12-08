package ai.digital.integration.server.deploy.tasks.kube.scanning

import ai.digital.integration.server.common.util.KubeScanningUtil
import ai.digital.integration.server.common.util.ProcessUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class CheckingOutKubeBenchTask : DefaultTask() {

    companion object {
        const val NAME = "checkingOutKubeBench"
    }

    @TaskAction
    fun launch() {
        project.logger.lifecycle("Checking out kube-bench repo")
        val tagVersion = KubeScanningUtil.getKubeScanner(project).kubeBenchTagVersion
        cloneRepository(tagVersion)
    }

    private fun cloneRepository(tagVersion: String) {
        var tag = ""
        if (tagVersion != "latest") {
            tag = "--branch " +
                    "$tagVersion "
        }
        ProcessUtil.executeCommand(project,
            "git clone " +
                    tag +
                    "https://github.com/aquasecurity/kube-bench.git" +
                    " \"${KubeScanningUtil.getKubeBenchDir(project)}\"",
            logOutput = KubeScanningUtil.getKubeScanner(project).logOutput)

    }
}
