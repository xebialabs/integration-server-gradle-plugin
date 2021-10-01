package ai.digital.integration.server.tasks.cli

import ai.digital.integration.server.domain.Cli
import ai.digital.integration.server.tasks.TlsApplicationConfigurationOverrideTask
import ai.digital.integration.server.util.CliUtil
import ai.digital.integration.server.util.ServerUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class RunCliTask extends DefaultTask {
    public static String NAME = "runCli"

    RunCliTask() {
        def dependencies = [
                CliCleanDefaultExtTask.NAME,
                CliOverlaysTask.NAME,
                DownloadAndExtractCliDistTask.NAME,
        ]

        if (ServerUtil.isTls(project)) {
            dependencies += [TlsApplicationConfigurationOverrideTask.NAME ]
        }

        this.configure {
            group = PLUGIN_GROUP
            dependsOn(dependencies)
        }
    }

    private def executeScripts(Cli cli) {
        project.logger.lifecycle("Executing cli scripts ....")
        CliUtil.executeScripts(project, cli.getFilesToExecute(), "cli")
    }

    @TaskAction
    void launch() {
        project.logger.lifecycle("Running a CLI provision script on the Deploy server.")
        executeScripts(CliUtil.getCli(project))
    }

}
