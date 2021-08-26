package ai.digital.integration.server.tasks.cli

import ai.digital.integration.server.domain.Cli
import ai.digital.integration.server.domain.Server
import ai.digital.integration.server.util.CliUtil
import ai.digital.integration.server.util.EnvironmentUtil
import ai.digital.integration.server.util.ProcessUtil
import ai.digital.integration.server.util.ServerUtil
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
        Server server = ServerUtil.getServer(project)

        cli.getFilesToExecute().each { File scriptSource ->

            def params = [
                    "-context", server.contextRoot,
                    "-expose-proxies",
                    "-password", "admin",
                    "-port", server.httpPort.toString(),
                    "-socketTimeout", cli.socketTimeout.toString(),
                    "-source", scriptSource.absolutePath,
                    "-username", "admin",
            ]

            def workDir = CliUtil.getCliBin(project)

            project.logger.lifecycle("Running provision script ${scriptSource} from working dir ${workDir} with parameters:${params}")

            ProcessUtil.exec([
                    command    : "cli",
                    environment: EnvironmentUtil.getCliEnv(cli),
                    params     : params,
                    redirectTo : CliUtil.getCliLogFile(project),
                    wait       : true,
                    workDir    : workDir
            ])
        }
    }

    @TaskAction
    void launch() {
        project.logger.lifecycle("Running a CLI provision script on the Deploy server.")
        executeScripts(CliUtil.getCli(project))
    }

}
