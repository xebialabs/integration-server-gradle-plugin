package ai.digital.integration.server

import ai.digital.integration.server.common.KubeScannerRegistry
import ai.digital.integration.server.common.TaskRegistry
import ai.digital.integration.server.deploy.DeployTaskRegistry
import ai.digital.integration.server.deploy.internals.DeployConfigurationsUtil.Companion.DEPLOY_SERVER
import ai.digital.integration.server.deploy.internals.DeployConfigurationsUtil.Companion.registerConfigurations
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil.Companion.createDeployExtension
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil.Companion.initialize
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil.Companion.isDeployServerDefined
import ai.digital.integration.server.release.ReleaseTaskRegistry
import ai.digital.integration.server.release.internals.ReleaseExtensionUtil.Companion.createReleaseExtension
import ai.digital.integration.server.release.util.ReleaseServerUtil
import ai.digital.integration.server.release.util.ReleaseServerUtil.Companion.isReleaseServerDefined
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.closureOf

class IntegrationServerPlugin : Plugin<Project> {

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
            }

            if (isReleaseServerDefined(project)) {
                initialize(project)
                ReleaseTaskRegistry.register(project)
            }

            TaskRegistry.register(project)
            KubeScannerRegistry.register(project)
        }
    }
}
