package ai.digital.integration.server.release.tasks.cluster.helm

import ai.digital.integration.server.common.cluster.helm.HelmHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.util.GitUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class ProvideReleaseKubernetesHelmChartTask : DefaultTask() {

    companion object {
        const val NAME = "provideReleaseKubernetesHelmChart"
    }

    @TaskAction
    fun launch() {
        val helmHelper = HelmHelper.getHelmHelper(project, ProductName.RELEASE)
        val branch = helmHelper.getProvider().helmBranch.get()
        project.logger.lifecycle("Checking out xl-release-kubernetes-helm-chart branch $branch")
        cloneRepository(branch)

    }

    private fun cloneRepository(branch: String) {
        GitUtil.checkout("xl-release-kubernetes-helm-chart", project.layout.buildDirectory.get().asFile.toPath(), branch)
    }
}
