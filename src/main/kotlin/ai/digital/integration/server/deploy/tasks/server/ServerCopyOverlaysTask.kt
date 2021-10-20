package ai.digital.integration.server.deploy.tasks.server

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.deploy.util.DeployServerUtil
import ai.digital.integration.server.deploy.util.DeployExtensionUtil
import ai.digital.integration.server.common.util.OverlaysUtil
import org.gradle.api.DefaultTask

open class ServerCopyOverlaysTask : DefaultTask() {

    companion object {
        const val NAME = "serverCopyOverlays"
    }

    init {
        this.group = PLUGIN_GROUP
        this.mustRunAfter(DownloadAndExtractServerDistTask.NAME)
        this.mustRunAfter(CentralConfigurationTask.NAME)
        this.mustRunAfter(CopyServerBuildArtifactsTask.NAME)
        this.finalizedBy(CheckUILibVersionsTask.NAME)

        project.afterEvaluate {
            val server = DeployServerUtil.getServer(project)
            project.logger.lifecycle("Copying overlays on Deploy server ${server.name}")

            OverlaysUtil.addDatabaseDependency(project, server)
            OverlaysUtil.addMqDependency(project, server)

            server.overlays.forEach { overlay ->
                OverlaysUtil.defineOverlay(
                    project,
                    this,
                    DeployServerUtil.getServerWorkingDir(project),
                    DeployExtensionUtil.DEPLOY_IS_EXTENSION_NAME,
                    overlay,
                    arrayListOf())
            }
        }
    }
}
