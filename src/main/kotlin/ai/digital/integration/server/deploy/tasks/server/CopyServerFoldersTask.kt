package ai.digital.integration.server.deploy.tasks.server

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.deploy.internals.CopyDeployServerUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class CopyServerFoldersTask : DefaultTask() {

    companion object {
        const val NAME = "copyServerFolders"
    }

    init {
        this.group = PLUGIN_GROUP
        this.dependsOn(DownloadAndExtractServerDistTask.NAME)
    }

    @TaskAction
    fun launch() {
        val server = DeployServerUtil.getServer(project)
        CopyDeployServerUtil.execute(
            server.copyFolders,
            DeployServerUtil.getServerWorkingDir(project))
    }
}
