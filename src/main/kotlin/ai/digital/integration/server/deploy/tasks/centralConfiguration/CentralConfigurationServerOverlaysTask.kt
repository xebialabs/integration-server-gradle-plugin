package ai.digital.integration.server.deploy.tasks.centralConfiguration

import ai.digital.integration.server.common.constant.PluginConstant
import ai.digital.integration.server.deploy.internals.CentralConfigurationServerUtil
import ai.digital.integration.server.common.util.OverlaysUtil
import org.gradle.api.DefaultTask

open class CentralConfigurationServerOverlaysTask : DefaultTask() {

    companion object {
        const val NAME = "centralConfigurationServerOverlays"
        const val PREFIX = "ccServerOverlays"
    }

    init {
        this.group = PluginConstant.PLUGIN_GROUP
        this.onlyIf {
            CentralConfigurationServerUtil.hasCentralConfigurationServer(project)
        }

        this.mustRunAfter(PrepareCentralConfigurationServerTask.NAME)
        this.mustRunAfter(DownloadAndExtractCentralConfigurationServerDistTask.NAME)
        val currentTask = this

        val configureOverlays = {
            val server = CentralConfigurationServerUtil.getCentralConfigurationServer(project)
            project.logger.lifecycle("Copying overlays on central configuration server ${server.name}")
            server.overlays.forEach { overlay ->
                OverlaysUtil.defineOverlay(project,
                        currentTask,
                        CentralConfigurationServerUtil.getServerPath(project, server).toString(),
                        PREFIX,
                        overlay,
                        arrayListOf("${DownloadAndExtractCentralConfigurationServerDistTask.NAME}Exec"))
            }
        }

        if (project.state.executed) {
            configureOverlays()
        } else {
            project.afterEvaluate {
                configureOverlays()
            }
        }
    }
}