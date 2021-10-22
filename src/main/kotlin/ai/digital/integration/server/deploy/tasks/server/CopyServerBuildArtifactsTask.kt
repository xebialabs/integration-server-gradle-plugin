package ai.digital.integration.server.deploy.tasks.server

import ai.digital.integration.server.common.constant.PluginConstant.PLUGIN_GROUP
import ai.digital.integration.server.common.util.CopyBuildArtifactsUtil
import ai.digital.integration.server.deploy.internals.DeployServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class CopyServerBuildArtifactsTask : DefaultTask() {

    companion object {
        const val NAME = "copyServerBuildArtifacts"
    }

    init {
        this.group = PLUGIN_GROUP
        this.dependsOn(DownloadAndExtractServerDistTask.NAME)
    }

    @TaskAction
    fun launch() {
        DeployServerUtil.getServers(project)
                .forEach { server ->
                    CopyBuildArtifactsUtil.execute(project,
                            server.copyBuildArtifacts,
                            DeployServerUtil.getServerWorkingDir(project, server))
                }

    }
}
