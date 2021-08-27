package ai.digital.integration.server.tasks.cli

import ai.digital.integration.server.domain.Cli
import ai.digital.integration.server.util.CliUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP

class RunCliTask extends DefaultTask {
    static NAME = "runCli"

    RunCliTask() {
        def dependencies = [
                DownloadAndExtractCliDistTask.NAME,
                CliOverlaysTask.NAME
        ]

        this.configure {
            group = PLUGIN_GROUP
            dependsOn(dependencies)
        }
    }

    private def executeScripts(Cli cli) {
        project.logger.lifecycle("Executing cli scripts ....")
        cli.getFilesToExecute().each { File scriptSource -> CliUtil.executeScript(project, scriptSource) }
    }

    @TaskAction
    void launch() {
        project.logger.lifecycle("Running a CLI provision script on the Deploy server.")
        executeScripts(CliUtil.getCli(project))
    }

}
