package ai.digital.integration.server.tasks

import ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.util.CopyBuildArtifactsUtil
import ai.digital.integration.server.util.DeployServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class CopyServerBuildArtifactsTask : DefaultTask() {

    companion object {
        @JvmStatic
        val NAME = "copyServerBuildArtifacts"
    }

    init {
        this.group = PLUGIN_GROUP
        this.dependsOn(DownloadAndExtractServerDistTask.NAME)
    }

    @TaskAction
    fun launch() {
        val server = DeployServerUtil.getServer(project)
        CopyBuildArtifactsUtil.execute(project,
            server.copyBuildArtifacts,
            DeployServerUtil.getServerWorkingDir(project))
    }
}
