package ai.digital.integration.server.deploy.tasks.cluster.k8sinstaller.scanning

import ai.digital.integration.server.common.util.ProcessUtil
import ai.digital.integration.server.deploy.internals.KubeBenchUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class CheckingOutKubeBenchTask : DefaultTask() {

    companion object {
        const val NAME = "checkingOutKubeBenchTask"
    }

    @TaskAction
    fun launch() {
        project.logger.lifecycle("Checking out kube-bench repo")
        cloneRepository()
    }

    private fun cloneRepository() {
        ProcessUtil.executeCommand(project,
                "git clone https://github.com/aquasecurity/kube-bench.git ${KubeBenchUtil.getKubeBenchDir(project)}")
    }
}
