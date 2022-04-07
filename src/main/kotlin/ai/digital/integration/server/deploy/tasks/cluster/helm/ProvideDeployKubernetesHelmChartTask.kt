package ai.digital.integration.server.deploy.tasks.cluster.helm

import ai.digital.integration.server.common.cluster.helm.HelmHelper
import ai.digital.integration.server.common.cluster.operator.OperatorHelper
import ai.digital.integration.server.common.constant.ProductName
import ai.digital.integration.server.common.util.GitUtil
import ai.digital.integration.server.common.util.IntegrationServerUtil
import ai.digital.integration.server.deploy.internals.DeployConfigurationsUtil
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskAction
import java.io.File

open class ProvideDeployKubernetesHelmChartTask : DefaultTask() {

    companion object {
        const val NAME = "provideDeployKubernetesHelmChart"
    }

    @TaskAction
    fun launch() {
        val helmHelper = HelmHelper.getHelmHelper(project, ProductName.DEPLOY)
        val branch = helmHelper.getProvider().helmBranch.get()
        project.logger.lifecycle("Checking out xl-deploy-kubernetes-helm-chart branch $branch")
        cloneRepository(branch)

    }

    private fun cloneRepository(branch: String) {
        GitUtil.checkout("xl-deploy-kubernetes-helm-chart", project.buildDir.toPath(), branch)
    }
}
