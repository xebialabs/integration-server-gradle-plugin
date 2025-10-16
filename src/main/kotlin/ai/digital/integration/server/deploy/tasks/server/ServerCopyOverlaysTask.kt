package ai.digital.integration.server.deploy.tasks.server

import ai.digital.integration.server.common.cluster.util.OperatorUtil
import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.CacheUtil
import ai.digital.integration.server.common.util.OverlaysUtil
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import ai.digital.integration.server.deploy.tasks.cli.DownloadAndExtractCliDistTask
import org.gradle.api.DefaultTask

open class ServerCopyOverlaysTask : DefaultTask() {

    companion object {
        const val NAME = "serverCopyOverlays"
    }

    init {
        this.group = PLUGIN_GROUP

        if (!OperatorUtil(project).isClusterEnabled()) {
            this.mustRunAfter(CentralConfigurationTask.NAME)
            this.mustRunAfter(CopyServerBuildArtifactsTask.NAME)
            this.mustRunAfter(DownloadAndExtractServerDistTask.NAME)
            this.finalizedBy(CheckUILibVersionsTask.NAME)
        }
        val currentTask = this

        val configureOverlays = {
            DeployServerUtil.getServers(project).forEach { server ->
                project.logger.lifecycle("Copying overlays on Deploy server ${server.name}")

                // Add overlays without modifying configuration during task construction
                OverlaysUtil.addDatabaseOverlayOnly(project, server)
                OverlaysUtil.addMqOverlayOnly(project, server)
                if (CacheUtil.isCacheEnabled(project)) {
                    OverlaysUtil.addCacheOverlayOnly(project, server)
                }

                server.overlays.forEach { overlay ->
                    OverlaysUtil.defineOverlay(project,
                        currentTask,
                        DeployServerUtil.getServerWorkingDir(project, server),
                        DeployExtensionUtil.DEPLOY_IS_EXTENSION_NAME,
                        overlay,
                        if (DeployServerUtil.isDistDownloadRequired(project,
                                server)
                        ) arrayListOf("${DownloadAndExtractServerDistTask.NAME}${server.name}", DownloadAndExtractCliDistTask.NAME)
                        else arrayListOf(),
                        server.name)
                }
            }
        }

        if (project.state.executed) {
            configureOverlays()
        } else {
            project.afterEvaluate {
                configureOverlays()
                // Add configuration dependencies at the end of configuration phase
                DeployServerUtil.getServers(project).forEach { server ->
                    project.logger.lifecycle("Adding configuration dependencies for Deploy server ${server.name}")
                    OverlaysUtil.addAllConfigurationDependencies(project, server)
                }
            }
        }
    }
}
