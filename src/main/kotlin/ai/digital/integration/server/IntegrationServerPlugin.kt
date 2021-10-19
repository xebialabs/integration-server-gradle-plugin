package ai.digital.integration.server

import ai.digital.integration.server.common.TaskRegistry
import ai.digital.integration.server.common.util.DbUtil.Companion.getDatabase
import ai.digital.integration.server.common.util.TaskUtil.Companion.dontFailOnException
import ai.digital.integration.server.deploy.DeployTaskRegistry
import ai.digital.integration.server.deploy.tasks.ApplicationConfigurationOverrideTask
import ai.digital.integration.server.deploy.util.DeployConfigurationsUtil.Companion.DEPLOY_SERVER
import ai.digital.integration.server.deploy.util.DeployConfigurationsUtil.Companion.registerConfigurations
import ai.digital.integration.server.deploy.util.DeployExtensionUtil.Companion.createDeployExtension
import ai.digital.integration.server.deploy.util.DeployExtensionUtil.Companion.initialize
import ai.digital.integration.server.deploy.util.DeployServerUtil
import ai.digital.integration.server.deploy.util.DeployServerUtil.Companion.isDeployServerDefined
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
        val database = getDatabase(project)
        project.plugins.apply("derby-ns")

        val derbyExtension = project.extensions.getByName("derby") as DerbyExtension
        derbyExtension.dataDir = workDir
        derbyExtension.port = database.derbyPort.get()

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
                DeployTaskRegistry.register(project, serverConfig)
                applyPlugins(project, DeployServerUtil.getServerWorkingDir(project))
            }

            if (isReleaseServerDefined(project)) {
                initialize(project)
                ReleaseTaskRegistry.register(project)
                applyPlugins(project, ReleaseServerUtil.getServerWorkingDir(project))
            }

            TaskRegistry.register(project)
        }
    }
}
