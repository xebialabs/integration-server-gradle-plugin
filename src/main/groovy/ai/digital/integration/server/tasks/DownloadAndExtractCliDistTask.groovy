package ai.digital.integration.server.tasks

import ai.digital.integration.server.util.ServerUtil
import org.gradle.api.tasks.Copy

import static ai.digital.integration.server.constant.PluginConstant.DIST_DESTINATION_NAME
import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import static ai.digital.integration.server.util.ConfigurationsUtil.SERVER_CLI_DIST

class DownloadAndExtractCliDistTask extends Copy {
    static NAME = "downloadAndExtractCli"

    DownloadAndExtractCliDistTask() {
        this.configure {
            group = PLUGIN_GROUP
            def server = ServerUtil.getServer(project)

            if (ServerUtil.isDistDownloadRequired(project)) {
                project.logger.lifecycle("Downloading and extracting the CLI.")
                project.buildscript.dependencies.add(
                        SERVER_CLI_DIST,
                        "com.xebialabs.deployit:xl-deploy-base:${server.version}:cli@zip"
                )
                from { project.zipTree(project.buildscript.configurations.getByName(SERVER_CLI_DIST).singleFile) }
                into { project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString() }
            }
        }
    }
}
