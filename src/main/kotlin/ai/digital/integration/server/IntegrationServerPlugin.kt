package ai.digital.integration.server

import ai.digital.integration.server.common.KubeScannerRegistry
import ai.digital.integration.server.common.TaskRegistry
import ai.digital.integration.server.common.util.DbUtil.Companion.getPort
import ai.digital.integration.server.common.util.TaskUtil.Companion.dontFailOnException
import ai.digital.integration.server.deploy.DeployTaskRegistry
import ai.digital.integration.server.deploy.tasks.server.ApplicationConfigurationOverrideTask
import ai.digital.integration.server.deploy.internals.DeployConfigurationsUtil.Companion.DEPLOY_SERVER
import ai.digital.integration.server.deploy.internals.DeployConfigurationsUtil.Companion.registerConfigurations
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil.Companion.createDeployExtension
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil.Companion.initialize
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil.Companion.isDeployServerDefined
import ai.digital.integration.server.release.ReleaseTaskRegistry
import ai.digital.integration.server.release.util.ReleaseExtensionUtil.Companion.createReleaseExtension
import ai.digital.integration.server.release.util.ReleaseServerUtil
import ai.digital.integration.server.release.util.ReleaseServerUtil.Companion.isReleaseServerDefined
import com.xebialabs.gradle.plugins.derby.DerbyExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.closureOf

class IntegrationServerPlugin : Plugin<Project> {

    private fun applyDerbyPlugin(project: Project, workDir: String): Task {

        project.plugins.apply("derby-ns")

        val derbyExtension = project.extensions.getByName("derby") as DerbyExtension
        derbyExtension.dataDir.convention(workDir)
        derbyExtension.port.convention(getPort(project))
        derbyExtension.externalProcess.convention(true)

        val startDerbyTask = project.tasks.getByName("derbyStart")
        val stopDerbyTask = project.tasks.getByName("derbyStop")

        dontFailOnException(stopDerbyTask)
        stopDerbyTask.actions.forEach { action ->
            return startDerbyTask.doFirst(action)
        }
        return startDerbyTask.mustRunAfter(ApplicationConfigurationOverrideTask.NAME)
    }

    private fun applyPlugins(project: Project, workDir: String) {
        applyDerbyPlugin(project, workDir)
    }

    override fun apply(project: Project) {
        val serverConfig = project.configurations.create(DEPLOY_SERVER)
        registerConfigurations(project)
        project.configure(project, closureOf<Project> {
            createDeployExtension(project)
            createReleaseExtension(project)
        })
        project.afterEvaluate {
            if (isDeployServerDefined(project)) {
                initialize(project)
                applyPlugins(project, DeployServerUtil.getServerWorkingDir(project))
                DeployTaskRegistry.register(project, serverConfig)
            }

            if (isReleaseServerDefined(project)) {
                initialize(project)
                applyPlugins(project, ReleaseServerUtil.getServerWorkingDir(project))
                ReleaseTaskRegistry.register(project)
            }

            TaskRegistry.register(project)
            KubeScannerRegistry.register(project)
        }
    }
}
