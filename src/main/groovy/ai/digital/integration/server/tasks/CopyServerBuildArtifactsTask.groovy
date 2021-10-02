package ai.digital.integration.server.tasks

import ai.digital.integration.server.domain.Server
import ai.digital.integration.server.util.CopyBuildArtifactsUtil
import ai.digital.integration.server.util.DeployServerUtil
import ai.digital.integration.server.util.ServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class CopyServerBuildArtifactsTask extends DefaultTask {
    public static String NAME = "copyServerBuildArtifacts"

    CopyServerBuildArtifactsTask() {
        def dependencies = [
                DownloadAndExtractServerDistTask.NAME
        ]

        this.configure { ->
            group = PLUGIN_GROUP
            dependsOn(dependencies)
        }
    }

    @TaskAction
    void launch() {
        Server server = DeployServerUtil.getServer(project)
        CopyBuildArtifactsUtil.execute(project, server.copyBuildArtifacts, DeployServerUtil.getServerWorkingDir(project))
    }
}
