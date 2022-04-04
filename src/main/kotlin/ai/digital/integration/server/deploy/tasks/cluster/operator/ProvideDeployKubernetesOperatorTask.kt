package ai.digital.integration.server.deploy.tasks.cluster.operator

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

open class ProvideDeployKubernetesOperatorTask : DefaultTask() {

    companion object {
        const val NAME = "provideDeployKubernetesOperator"
    }

    init {
        if (DeployExtensionUtil.getExtension(project).clusterProfiles.operator().activeProviderName.isPresent) {
            val operatorHelper = OperatorHelper.getOperatorHelper(project, ProductName.DEPLOY)
            if (operatorHelper.getProvider().operatorPackageVersion.isPresent) {
                project.buildscript.dependencies.add(
                    DeployConfigurationsUtil.OPERATOR_DIST,
                    "ai.digital.deploy.operator:${operatorHelper.getProviderHomePath()}:${operatorHelper.getProvider().operatorPackageVersion.get()}@zip"
                )

                val taskName = "downloadAndExtractOperator${operatorHelper.getProviderHomePath()}"
                val task = project.tasks.register(taskName, Copy::class.java) {
                    from(project.zipTree(project.buildscript.configurations.getByName(DeployConfigurationsUtil.OPERATOR_DIST).singleFile))
                    into(operatorHelper.getProviderHomeDir())
                }
                this.dependsOn(task)
            }
        } else {
            project.logger.warn("Active provider name is not set - ProvideDeployKubernetesOperatorTask")
        }
    }

    @TaskAction
    fun launch() {
        val operatorHelper = OperatorHelper.getOperatorHelper(project, ProductName.DEPLOY)
        if (operatorHelper.getProvider().operatorBranch.isPresent) {
            // it needs to be aligned with operatorImage default value
            val branch = operatorHelper.getProvider().operatorBranch.get()
            project.logger.lifecycle("Checking out xl-deploy-kubernetes-operator branch $branch")
            cloneRepository(branch)
        } else if (operatorHelper.getProvider().operatorPackageVersion.isPresent) {
            // it needs to be aligned with operatorImage default value
            val version = operatorHelper.getProvider().operatorPackageVersion.get()
            project.logger.lifecycle("Downloading package xl-deploy-kubernetes-operator version $version")
        } else {
            // use the current repo
            project.logger.lifecycle("Using current repository as operator source")
            copyCurrentRepository(operatorHelper)
        }
    }

    private fun cloneRepository(branch: String) {
        GitUtil.checkout("xl-deploy-kubernetes-operator", project.buildDir.toPath(), branch)
    }

    private fun copyCurrentRepository(operatorHelper: OperatorHelper) {
        val providerHomePath = operatorHelper.getProviderHomePath()
        project.rootDir.resolve(providerHomePath).copyRecursively(File(operatorHelper.getProviderHomeDir()), true)
    }
}
