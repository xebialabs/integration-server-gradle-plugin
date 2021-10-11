package ai.digital.integration.server.tasks

import ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.util.DeployServerUtil
import ai.digital.integration.server.util.ExtensionUtil
import ai.digital.integration.server.util.OverlaysUtil
import org.gradle.api.DefaultTask

abstract class CopyOverlaysTask : DefaultTask() {

    companion object {
        @JvmStatic
        val NAME = "copyOverlays"
    }

    init {
        this.group = PLUGIN_GROUP
        this.mustRunAfter(DownloadAndExtractServerDistTask.NAME)
        this.mustRunAfter(CentralConfigurationTask.NAME)
        this.mustRunAfter(CopyServerBuildArtifactsTask.NAME)
        this.finalizedBy("checkUILibVersions")

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
                    ExtensionUtil.IS_EXTENSION_NAME,
                    overlay,
                    arrayListOf())
            }
        }
    }
}
