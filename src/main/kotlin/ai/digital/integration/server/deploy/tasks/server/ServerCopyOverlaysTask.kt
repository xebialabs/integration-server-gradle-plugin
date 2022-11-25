package ai.digital.integration.server.deploy.tasks.server

import ai.digital.integration.server.common.cluster.util.OperatorUtil
import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.CacheUtil
import ai.digital.integration.server.common.util.OverlaysUtil
import ai.digital.integration.server.deploy.internals.DeployExtensionUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
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

        project.afterEvaluate {
            DeployServerUtil.getServers(project).forEach { server ->
                project.logger.lifecycle("Copying overlays on Deploy server ${server.name}")

                OverlaysUtil.addDatabaseDependency(project, server)
                OverlaysUtil.addMqDependency(project, server)
                if (CacheUtil.isCacheEnabled(project)) {
                    OverlaysUtil.addCacheDependency(project, server)
                }

                server.overlays.forEach { overlay ->
                    OverlaysUtil.defineOverlay(project,
                        currentTask,
                        DeployServerUtil.getServerWorkingDir(project, server),
                        DeployExtensionUtil.DEPLOY_IS_EXTENSION_NAME,
                        overlay,
                        if (DeployServerUtil.isDistDownloadRequired(project,
                                server)
                        ) arrayListOf("${DownloadAndExtractServerDistTask.NAME}${server.name}")
                        else arrayListOf(),
                        server.name)
                }
            }
        }
    }
}
