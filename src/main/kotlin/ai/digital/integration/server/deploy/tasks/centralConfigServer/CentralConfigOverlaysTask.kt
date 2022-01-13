package ai.digital.integration.server.deploy.tasks.centralConfigServer

import ai.digital.integration.server.common.constant.PluginConstant
import org.gradle.api.DefaultTask

open class CentralConfigOverlaysTask : DefaultTask() {
    init {
        this.group = PluginConstant.PLUGIN_GROUP
        this.mustRunAfter(DownloadAndExtractCCDistTask.NAME)
    }
    companion object {
        const val NAME = "centralConfigOverlays"
        const val PREFIX = "centralConfig"
    }
}
