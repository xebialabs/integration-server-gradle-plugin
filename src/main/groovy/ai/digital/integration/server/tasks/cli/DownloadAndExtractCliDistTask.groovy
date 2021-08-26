package ai.digital.integration.server.tasks.cli

import ai.digital.integration.server.util.CliUtil
import ai.digital.integration.server.util.IntegrationServerUtil
import org.gradle.api.tasks.Copy

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import static ai.digital.integration.server.util.ConfigurationsUtil.SERVER_CLI_DIST

class DownloadAndExtractCliDistTask extends Copy {
    static NAME = "downloadAndExtractCli"

    DownloadAndExtractCliDistTask() {
        this.configure {
            group = PLUGIN_GROUP
            def version = CliUtil.getCli(project).version

            project.logger.lifecycle("Downloading and extracting the CLI ${version}.")

            project.buildscript.dependencies.add(
                    SERVER_CLI_DIST,
                    "com.xebialabs.deployit:xl-deploy-base:${version}:cli@zip"
            )
            from { project.zipTree(project.buildscript.configurations.getByName(SERVER_CLI_DIST).singleFile) }
            into { IntegrationServerUtil.getDist(project) }
        }
    }
}
