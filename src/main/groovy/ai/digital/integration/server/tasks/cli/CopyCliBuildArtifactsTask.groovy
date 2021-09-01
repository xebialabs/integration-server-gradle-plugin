package ai.digital.integration.server.tasks.cli

import ai.digital.integration.server.domain.Cli
import ai.digital.integration.server.util.CliUtil
import ai.digital.integration.server.util.CopyBuildArtifactsUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class CopyCliBuildArtifactsTask extends DefaultTask {
    static NAME = "copyCliBuildArtifacts"

    CopyCliBuildArtifactsTask() {
        def dependencies = [
                DownloadAndExtractCliDistTask.NAME
        ]

        this.configure { ->
            group = PLUGIN_GROUP
            dependsOn(dependencies)
        }
    }

    @TaskAction
    void launch() {
        Cli cli = CliUtil.getCli(project)
        CopyBuildArtifactsUtil.execute(project, cli.copyBuildArtifacts, CliUtil.getWorkingDir(project))
    }
}
