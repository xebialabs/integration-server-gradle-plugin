package ai.digital.integration.server.tasks

import ai.digital.integration.server.domain.Server
import ai.digital.integration.server.util.ServerUtil
import org.gradle.api.tasks.Copy

import static ai.digital.integration.server.constant.PluginConstant.PLUGIN_GROUP
import static ai.digital.integration.server.util.ConfigurationsUtil.SERVER_DIST

class DownloadAndExtractServerDistTask extends Copy {
    static NAME = "downloadAndExtractServer"

    DownloadAndExtractServerDistTask() {
        this.configure {
            def server = ServerUtil.getServer(project)

            group = PLUGIN_GROUP

            if (isDownloadRequired(server)) {
                project.logger.lifecycle("Downloading and extracting the server.")
                project.buildscript.dependencies.add(
                        SERVER_DIST,
                        "com.xebialabs.deployit:xl-deploy-base:${server.version}:server@zip"
                )
                from { project.zipTree(project.buildscript.configurations.getByName(SERVER_DIST).singleFile) }
                into { ServerUtil.getServerDistFolder(project) }
            }
        }
    }

    private static def isDownloadRequired(Server server) {
        server.runtimeDirectory == null
    }
}
